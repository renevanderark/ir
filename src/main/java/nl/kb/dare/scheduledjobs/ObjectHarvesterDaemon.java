package nl.kb.dare.scheduledjobs;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.ErrorReporter;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.objectharvester.ObjectHarvester;
import nl.kb.dare.objectharvester.ObjectHarvesterOperations;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.dare.websocket.socketupdate.RecordFetcherUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class ObjectHarvesterDaemon extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHarvesterDaemon.class);
    private static AtomicInteger runningWorkers = new AtomicInteger(0);

    private final RecordDao recordDao;
    private final RepositoryDao repositoryDao;
    private final SocketNotifier socketNotifier;
    private final RecordReporter recordReporter;
    private final ErrorReportDao errorReportDao;
    private final ErrorReporter errorReporter;
    private final Integer maxParallelDownloads;
    private final Long downloadQueueFillDelayMs;
    private ObjectHarvesterOperations getRecordOperations;

    public enum RunState {
        RUNNING, DISABLING, DISABLED
    }

    private RunState runState;

    public ObjectHarvesterDaemon(RecordDao recordDao, RepositoryDao repositoryDao,
                                 SocketNotifier socketNotifier, RecordReporter recordReporter,
                                 ErrorReportDao errorReportDao, ErrorReporter errorReporter,
                                 Integer maxWorkers, Long downloadQueueFillDelayMs,
                                 ObjectHarvesterOperations objectHarvesterOperations) {

        this.recordDao = recordDao;
        this.repositoryDao = repositoryDao;
        this.socketNotifier = socketNotifier;
        this.recordReporter = recordReporter;
        this.errorReportDao = errorReportDao;
        this.errorReporter = errorReporter;
        this.maxParallelDownloads = maxWorkers;
        this.downloadQueueFillDelayMs = downloadQueueFillDelayMs;
        this.runState = RunState.DISABLED;

        this.getRecordOperations = objectHarvesterOperations;
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (runState == RunState.DISABLED || runState == RunState.DISABLING) {
            checkRunState();
            return;
        }

        final List<Record> pendingRecords = fetchNextRecords(maxParallelDownloads);
        final List<Thread> workers = Lists.newArrayList();

        for (Record record : pendingRecords) {
            startRecord(record);

            final Thread worker = new Thread(() -> {
                final Stopwatch timer = Stopwatch.createStarted();
                final ProcessStatus result = ObjectHarvester.getAndRun(
                        repositoryDao, record, getRecordOperations,
                        (ErrorReport errorReport) -> saveErrorReport(errorReport, record) // on error
                );

                finishRecord(record, result, timer.elapsed(TimeUnit.SECONDS));
                runningWorkers.getAndDecrement();
            });
            workers.add(worker);
            worker.start();
            runningWorkers.getAndIncrement();
        }
        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());


        if (runState == RunState.DISABLING) {
            for (Thread worker : workers) {
                worker.join();
            }
        }

        checkRunState();
    }

    private void checkRunState() {
        final RunState runStateBefore = runState;
        if (runState == RunState.DISABLED || runState == RunState.DISABLING){
            runState = runningWorkers.get() > 0 ? RunState.DISABLING : RunState.DISABLED;
        } else {
            runState = RunState.RUNNING;
        }

        if (runStateBefore != runState) {
            socketNotifier.notifyUpdate(new RecordFetcherUpdate(runState));
        }
    }

    private List<Record> fetchNextRecords(int limit) {
        final List<Record> result = Lists.newArrayList();
        final List<Integer> repositoryIds = repositoryDao.list().stream()
                .filter(Repository::getEnabled)
                .map(Repository::getId).collect(toList());

        // Prevents division by (near to) zero in next line.
        if (repositoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        final int dividedLimit = (int) Math.ceil(((float) limit / (float) repositoryIds.size()));

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

    public void enable() {
        runState = RunState.RUNNING;
        socketNotifier.notifyUpdate(new RecordFetcherUpdate(runState));
    }

    public void disable() {
        runState = RunState.DISABLING;
        socketNotifier.notifyUpdate(new RecordFetcherUpdate(runState));
    }

    private void startRecord(Record record) {
        record.setState(ProcessStatus.PROCESSING);
        recordDao.updateState(record);
    }

    private void finishRecord(Record record, ProcessStatus processStatus, long elapsed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finished record {} with status {}  in {} seconds", record.getKbObjId(), processStatus, elapsed);
        }
        record.setState(processStatus);
        recordDao.updateState(record);
        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }

    private void saveErrorReport(ErrorReport errorReport, Record record) {
        LOG.info("Failed to process record {} ({})", record.getOaiIdentifier(), errorReport.getUrl(), errorReport.getException());
        errorReportDao.insert(record.getId(), errorReport);
        socketNotifier.notifyUpdate(errorReporter.getStatusUpdate());
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, downloadQueueFillDelayMs, TimeUnit.MILLISECONDS);
    }
}
