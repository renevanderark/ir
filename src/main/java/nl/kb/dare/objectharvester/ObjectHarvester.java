package nl.kb.dare.objectharvester;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import nl.kb.dare.config.FileStorageGoal;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.ErrorReporter;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.manifest.ObjectResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static nl.kb.dare.config.FileStorageGoal.DONE;
import static nl.kb.dare.config.FileStorageGoal.REJECTED;


public class ObjectHarvester {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHarvester.class);
    private static final Map<Integer, Integer> failCounts = Collections.synchronizedMap(new HashMap<>());

    private final ObjectHarvesterOperations objectHarvesterOperations;
    private final RepositoryDao repositoryDao;
    private final RecordDao recordDao;
    private final ErrorReportDao errorReportDao;
    private final RecordReporter recordReporter;
    private final ErrorReporter errorReporter;
    private final SocketNotifier socketNotifier;
    private final Integer maxSequentialDownloadFailures;
    private ObjectHarvestErrorFlowHandler objectHarvestErrorFlowHandler;


    private ObjectHarvester(Builder builder) {

        this.repositoryDao = builder.repositoryDao;
        this.recordDao = builder.recordDao;
        this.errorReportDao = builder.errorReportDao;
        this.objectHarvesterOperations = builder.objectHarvesterOperations;
        this.recordReporter = builder.recordReporter;
        this.errorReporter = builder.errorReporter;
        this.socketNotifier = builder.socketNotifier;
        this.maxSequentialDownloadFailures = builder.maxSequentialDownloadFailures;
        this.objectHarvestErrorFlowHandler = builder.objectHarvestErrorFlowHandler;
    }

    public List<Thread> harvestNextPublications(Integer maxParallelDownloads, AtomicInteger runningWorkers) {
        final List<Record> pendingRecords = fetchNextRecords(maxParallelDownloads);
        final List<Thread> workers = Lists.newArrayList();

        for (Record record : pendingRecords) {
            startRecord(record);

            final Thread worker = new Thread(() -> {
                final Stopwatch timer = Stopwatch.createStarted();
                final Repository repositoryConfig = repositoryDao.findById(record.getRepositoryId());
                if (repositoryConfig != null) {
                    final ProcessStatus result = harvestPublication(
                            record,
                            repositoryConfig,
                            (ErrorReport errorReport) -> saveErrorReport(errorReport, record) // on error
                    );
                    finishRecord(record, result, timer.elapsed(TimeUnit.SECONDS));
                } else {
                    LOG.error("SEVERE! OaiRecord missing repository configuration in database: {}", record);
                    finishRecord(record, ProcessStatus.FAILED, timer.elapsed(TimeUnit.SECONDS));
                }
                runningWorkers.getAndDecrement();
            });
            workers.add(worker);
            worker.start();
            runningWorkers.getAndIncrement();
        }
        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
        return workers;
    }

    ProcessStatus harvestPublication(Record record, Repository repositoryConfig, Consumer<ErrorReport> onError) {

        final Optional<FileStorageHandle> processingStorageHandle = objectHarvesterOperations.getFileStorageHandle(
                FileStorageGoal.PROCESSING, getSuperSetFromSetName(repositoryConfig), record, onError);


        if (!processingStorageHandle.isPresent()) {
            return ProcessStatus.FAILED;
        }

        final FileStorageHandle handle = processingStorageHandle.get();
        final Optional<ObjectResource> metadataResource = objectHarvesterOperations.downloadMetadata(handle, record,
                repositoryConfig, onError);

        if (!metadataResource.isPresent()) {
            objectHarvesterOperations.moveToStorage(REJECTED, handle, getSuperSetFromSetName(repositoryConfig), record);
            return ProcessStatus.FAILED;
        }

        if (!objectHarvesterOperations.generateManifest(handle, onError)) {
            objectHarvesterOperations.moveToStorage(REJECTED, handle, getSuperSetFromSetName(repositoryConfig), record);
            return ProcessStatus.FAILED;
        }

        final List<ObjectResource> objectResources = objectHarvesterOperations.collectResources(handle, onError);
        if (!objectHarvesterOperations.downloadResources(handle, objectResources, onError)) {
            objectHarvesterOperations.moveToStorage(REJECTED, handle, getSuperSetFromSetName(repositoryConfig), record);
            return ProcessStatus.FAILED;
        }

        if (!objectHarvesterOperations
                .writeFilenamesAndChecksumsToMetadata(handle, objectResources, metadataResource.get(), onError)) {

            objectHarvesterOperations.moveToStorage(REJECTED, handle, getSuperSetFromSetName(repositoryConfig), record);
            return ProcessStatus.FAILED;
        }

        if (!objectHarvesterOperations.generateManifestChecksum(handle, onError)) {

            objectHarvesterOperations.moveToStorage(REJECTED, handle, getSuperSetFromSetName(repositoryConfig), record);
            return ProcessStatus.FAILED;
        }

        objectHarvesterOperations.moveToStorage(DONE, handle, getSuperSetFromSetName(repositoryConfig), record);
        return ProcessStatus.PROCESSED;
    }

    private String getSuperSetFromSetName(Repository repositoryConfig) {
        return repositoryConfig.getSet().replaceAll(":.*$", "");
    }

    private void startRecord(Record record) {
        record.setState(ProcessStatus.PROCESSING);
        recordDao.updateState(record);
    }

    private void finishRecord(Record record, ProcessStatus processStatus, long elapsed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished record {} with status {} in {} seconds", record.getOaiIdentifier(), processStatus, elapsed);
        }
        record.setState(processStatus);
        recordDao.updateState(record);
        if (processStatus == ProcessStatus.PROCESSED) {
            failCounts.put(record.getRepositoryId(), 0);
        } else {
            failCounts.put(record.getRepositoryId(),
                    failCounts.getOrDefault(record.getRepositoryId(), 0 ) + 1);
        }

        if (failCounts.getOrDefault(record.getRepositoryId(), 0) >= maxSequentialDownloadFailures) {
            objectHarvestErrorFlowHandler
                    .handleConsecutiveDownloadFailures(record.getRepositoryId(), maxSequentialDownloadFailures);
        }

        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }

    private void saveErrorReport(ErrorReport errorReport, Record record) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Failed to process record {} ({})", record.getOaiIdentifier(), errorReport.getUrl());
        }
        errorReportDao.insert(record.getId(), errorReport);
        socketNotifier.notifyUpdate(errorReporter.getStatusUpdate());
    }


    private List<Record> fetchNextRecords(int maxParallelDownloads) {
        final List<Record> result = Lists.newArrayList();
        final List<Integer> repositoryIds = repositoryDao.list().stream()
                .filter(Repository::getEnabled)
                .map(Repository::getId).collect(toList());

        // Prevents division by (near to) zero in next line.
        if (repositoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        final int dividedLimit = (int) Math.ceil(((float) maxParallelDownloads / (float) repositoryIds.size()));

        for (Integer repositoryId : repositoryIds) {
            final List<Record> processing = recordDao.fetchNextWithProcessStatusByRepositoryId(
                    ProcessStatus.PROCESSING.getCode(),
                    maxParallelDownloads,
                    repositoryId
            );

            int remainingSlots = dividedLimit - processing.size();
            if (remainingSlots <= 0) { continue; }
            final List<Record> pending = recordDao.fetchNextWithProcessStatusByRepositoryId(
                    ProcessStatus.PENDING.getCode(),
                    remainingSlots,
                    repositoryId
            );

            result.addAll(pending);
        }
        return result;
    }


    public static class Builder {
        private RepositoryDao repositoryDao;
        private RecordDao recordDao;
        private ErrorReportDao errorReportDao;
        private ObjectHarvesterOperations objectHarvesterOperations;
        private RecordReporter recordReporter;
        private ErrorReporter errorReporter;
        private SocketNotifier socketNotifier;
        private Integer maxSequentialDownloadFailures;
        private ObjectHarvestErrorFlowHandler objectHarvestErrorFlowHandler;

        public Builder setRepositoryDao(RepositoryDao repositoryDao) {
            this.repositoryDao = repositoryDao;
            return this;
        }

        public Builder setRecordDao(RecordDao recordDao) {
            this.recordDao = recordDao;
            return this;
        }

        public Builder setErrorReportDao(ErrorReportDao errorReportDao) {
            this.errorReportDao = errorReportDao;
            return this;
        }

        public Builder setObjectHarvesterOperations(ObjectHarvesterOperations objectHarvesterOperations) {
            this.objectHarvesterOperations = objectHarvesterOperations;
            return this;
        }

        public Builder setRecordReporter(RecordReporter recordReporter) {
            this.recordReporter = recordReporter;
            return this;
        }

        public Builder setErrorReporter(ErrorReporter errorReporter) {
            this.errorReporter = errorReporter;
            return this;
        }

        public Builder setSocketNotifier(SocketNotifier socketNotifier) {
            this.socketNotifier = socketNotifier;
            return this;
        }

        public Builder setMaxSequentialDownloadFailures(Integer maxSequentialDownloadFailures) {
            this.maxSequentialDownloadFailures = maxSequentialDownloadFailures;
            return this;
        }

        public Builder setObjectHarvestErrorFlowHandler(ObjectHarvestErrorFlowHandler objectHarvestErrorFlowHandler) {
            this.objectHarvestErrorFlowHandler = objectHarvestErrorFlowHandler;
            return this;
        }


        public ObjectHarvester create() {
            return new ObjectHarvester(this);
        }

    }
}
