package nl.kb.dare.jobs;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.jobs.getrecord.GetRecord;
import nl.kb.dare.model.SocketNotifier;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.ErrorReporter;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.filestorage.FileStorage;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.xslt.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class ScheduledOaiRecordFetcher extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledOaiRecordFetcher.class);
    private static AtomicInteger runningWorkers = new AtomicInteger(0);

    private final RecordDao recordDao;
    private final RepositoryDao repositoryDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final FileStorage fileStorage;
    private final XsltTransformer xsltTransformer;
    private final SocketNotifier socketNotifier;
    private final RecordReporter recordReporter;
    private final ErrorReportDao errorReportDao;
    private final ErrorReporter errorReporter;
    private final Integer maxParallelDownloads;
    private final Long downloadQueueFillDelayMs;

    public enum RunState {
        RUNNING, DISABLING, DISABLED
    }

    private RunState runState;

    public ScheduledOaiRecordFetcher(RecordDao recordDao, RepositoryDao repositoryDao,
                                     HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                     FileStorage fileStorage, XsltTransformer xsltTransformer,
                                     SocketNotifier socketNotifier, RecordReporter recordReporter,
                                     ErrorReportDao errorReportDao, ErrorReporter errorReporter,
                                     Integer maxWorkers, Long downloadQueueFillDelayMs) {
        this.recordDao = recordDao;
        this.repositoryDao = repositoryDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.fileStorage = fileStorage;
        this.xsltTransformer = xsltTransformer;
        this.socketNotifier = socketNotifier;
        this.recordReporter = recordReporter;
        this.errorReportDao = errorReportDao;
        this.errorReporter = errorReporter;
        this.maxParallelDownloads = maxWorkers;
        this.downloadQueueFillDelayMs = downloadQueueFillDelayMs;
        this.runState = RunState.DISABLED;
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

                ProcessStatus result = GetRecord.getAndRun(
                        repositoryDao, record, httpFetcher, responseHandlerFactory, fileStorage, xsltTransformer,
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
        runState = runState == RunState.DISABLED || runState == RunState.DISABLING
                ? runningWorkers.get() > 0 ? RunState.DISABLING : RunState.DISABLED
                : RunState.RUNNING;

        if (runStateBefore != runState) {
            socketNotifier.notifyUpdate(new RecordFetcherUpdate(runState));
        }
    }

    private List<Record> fetchNextRecords(int limit) {
        final List<Record> result = Lists.newArrayList();
        final List<Integer> repositoryIds = repositoryDao.list().stream()
                .filter(Repository::getEnabled)
                .map(Repository::getId).collect(toList());

        final int dividedLimit = (int) Math.ceil(((float) limit / (float) repositoryIds.size()));

        for (Integer repositoryId : repositoryIds) {
            final List<Record> processing = recordDao.fetchNextWithProcessStatusByRepositoryId(
                    ProcessStatus.PROCESSING.getCode(),
                    maxParallelDownloads,
                    repositoryId
            );

            int remainingLimit = dividedLimit - processing.size();
            if (remainingLimit <= 0) { continue; }
            final List<Record> pending = recordDao.fetchNextWithProcessStatusByRepositoryId(
                    ProcessStatus.PENDING.getCode(),
                    remainingLimit,
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
        LOG.error("Failed to process record {} ({})", record.getOaiIdentifier(), errorReport.getUrl(), errorReport.getException());
        errorReportDao.insert(record.getId(), errorReport);
        socketNotifier.notifyUpdate(errorReporter.getStatusUpdate());
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, downloadQueueFillDelayMs, TimeUnit.MILLISECONDS);
    }
}
