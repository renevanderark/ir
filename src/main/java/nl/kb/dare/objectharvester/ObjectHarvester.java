package nl.kb.dare.objectharvester;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;


public class ObjectHarvester {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHarvester.class);

    private final ObjectHarvesterOperations objectHarvesterOperations;
    private final RepositoryDao repositoryDao;
    private final RecordDao recordDao;
    private final ErrorReportDao errorReportDao;
    private final RecordReporter recordReporter;
    private final ErrorReporter errorReporter;
    private final SocketNotifier socketNotifier;

    public ObjectHarvester(RepositoryDao repositoryDao,
                           RecordDao recordDao,
                           ErrorReportDao errorReportDao,
                           ObjectHarvesterOperations objectHarvesterOperations,
                           RecordReporter recordReporter,
                           ErrorReporter errorReporter,
                           SocketNotifier socketNotifier) {

        this.repositoryDao = repositoryDao;
        this.recordDao = recordDao;
        this.errorReportDao = errorReportDao;
        this.objectHarvesterOperations = objectHarvesterOperations;
        this.recordReporter = recordReporter;
        this.errorReporter = errorReporter;
        this.socketNotifier = socketNotifier;
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

        final Optional<FileStorageHandle> fileStorageHandle = objectHarvesterOperations.getFileStorageHandle(record, onError);
        if (!fileStorageHandle.isPresent()) {
            return ProcessStatus.FAILED;
        }

        final FileStorageHandle handle = fileStorageHandle.get();
        final Optional<ObjectResource> metadataResource = objectHarvesterOperations.downloadMetadata(handle, record,
                repositoryConfig, onError);
        if (!metadataResource.isPresent()) {
            return ProcessStatus.FAILED;
        }

        if (!objectHarvesterOperations.generateManifest(handle, onError)) {
            return ProcessStatus.FAILED;
        }

        final List<ObjectResource> objectResources = objectHarvesterOperations.collectResources(handle, onError);
        if (!objectHarvesterOperations.downloadResources(handle, objectResources, onError)) {
            return ProcessStatus.FAILED;
        }

        if (!objectHarvesterOperations
                .writeFilenamesAndChecksumsToMetadata(handle, objectResources, metadataResource.get(), onError)) {
            return ProcessStatus.FAILED;
        }

        return ProcessStatus.PROCESSED;
    }

    private void startRecord(Record record) {
        record.setState(ProcessStatus.PROCESSING);
        recordDao.updateState(record);
    }

    private void finishRecord(Record record, ProcessStatus processStatus, long elapsed) {
        LOG.info("Finished record {} with status {} in {} seconds", record.getOaiIdentifier(), processStatus, elapsed);
        record.setState(processStatus);
        recordDao.updateState(record);
        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }

    private void saveErrorReport(ErrorReport errorReport, Record record) {
        LOG.info("Failed to process record {} ({})", record.getOaiIdentifier(), errorReport.getUrl());
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


}
