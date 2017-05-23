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
    private static final Integer MAX_WORKERS = 20;
    private static AtomicInteger runningWorkers = new AtomicInteger(0);

    private final RecordDao recordDao;
    private final RepositoryDao repositoryDao;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final FileStorage fileStorage;
    private final XsltTransformer xsltTransformer;
    private final SocketNotifier socketNotifier;
    private final RecordReporter recordReporter;

    public enum RunState {
        RUNNING, DISABLING, DISABLED
    }

    private RunState runState;

    public ScheduledOaiRecordFetcher(RecordDao recordDao, RepositoryDao repositoryDao,
                                     HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                     FileStorage fileStorage, XsltTransformer xsltTransformer,
                                     SocketNotifier socketNotifier, RecordReporter recordReporter) {
        this.recordDao = recordDao;
        this.repositoryDao = repositoryDao;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.fileStorage = fileStorage;
        this.xsltTransformer = xsltTransformer;
        this.socketNotifier = socketNotifier;
        this.recordReporter = recordReporter;
        this.runState = RunState.DISABLED;
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (runState == RunState.DISABLED || runState == RunState.DISABLING) {
            checkRunState();
            return;
        }

        final List<Record> pendingRecords = fetchNextRecords(MAX_WORKERS - runningWorkers.get());
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

        if (runState == RunState.DISABLING) {
            for (Thread worker : workers) {
                worker.join();
            }
        }

        checkRunState();
    }

    private void checkRunState() {
        runState = runState == RunState.DISABLED || runState == RunState.DISABLING
                ? runningWorkers.get() > 0 ? RunState.DISABLING : RunState.DISABLED
                : RunState.RUNNING;
        socketNotifier.notifyUpdate(new RecordFetcherUpdate(runState));
    }

    private List<Record> fetchNextRecords(int limit) {
        final List<Record> result = Lists.newArrayList();
        final List<Integer> repositoryIds = repositoryDao.list().stream()
                .filter(Repository::getEnabled)
                .map(Repository::getId).collect(toList());

        final int dividedLimit = new Double(Math.ceil(((float) limit / (float) repositoryIds.size()))).intValue();

        for (Integer repositoryId : repositoryIds) {
            final List<Record> processing = recordDao.fetchNextWithProcessStatusByRepositoryId(
                    ProcessStatus.PROCESSING.getCode(),
                    dividedLimit,
                    repositoryId
            );

            int remainingLimit = dividedLimit - processing.size();
            if (remainingLimit <= 0) { continue; }
            final List<Record> pending = recordDao.fetchNextWithProcessStatusByRepositoryId(
                    ProcessStatus.PENDING.getCode(),
                    dividedLimit,
                    repositoryId
            );

            result.addAll(pending);
            limit -= pending.size();
            if (limit <= 0) {
                return result;
            }
        }

        return result;
    }

    public void enable() {
        LOG.info("FETCH RECORDS ENABLED");
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
        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }

    private void finishRecord(Record record, ProcessStatus processStatus, long elapsed) {
        LOG.info("Finished record {} with status {}  in {} seconds", record.getKbObjId(), processStatus, elapsed);
        record.setState(processStatus);
        recordDao.updateState(record);
        socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());
    }

    private void saveErrorReport(ErrorReport errorReport, Record oaiRecord) {
        LOG.error("Failed to process record {} ({})", oaiRecord.getKbObjId(), errorReport.getUrl(), errorReport.getException());
//        errorReportDao.insertOaiRecordError(new OaiRecordErrorReport(errorReport, oaiRecord));
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 200, TimeUnit.MILLISECONDS);
    }
}
