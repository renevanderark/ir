package nl.kb.dare.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.RunState;
import nl.kb.dare.model.preproces.RecordBatchLoader;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.oaipmh.ListIdentifiers;

import java.util.function.Consumer;

public class RepositoryHarvester implements Runnable {
    private final Integer repositoryId;
    private final RepositoryController repositoryController;
    private final RecordBatchLoader recordBatchLoader;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final RepositoryDao repositoryDao;
    private final Consumer<RunState> stateChangeNotifier;
    private final Consumer<Exception> onException;

    private ListIdentifiers runningInstance = null;
    private RunState runState = RunState.WAITING;

    RepositoryHarvester(
            Integer repositoryId,
            RepositoryController repositoryController,
            RecordBatchLoader recordBatchLoader,
            HttpFetcher httpFetcher,
            ResponseHandlerFactory responseHandlerFactory,
            RepositoryDao repositoryDao,
            Consumer<RunState> stateChangeNotifier,
            Consumer<Exception> onException
    ) {

        this.repositoryId = repositoryId;
        this.repositoryController = repositoryController;
        this.recordBatchLoader = recordBatchLoader;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.repositoryDao = repositoryDao;
        this.stateChangeNotifier = stateChangeNotifier;
        this.onException = onException;
    }

    @Override
    public void run() {
        this.setRunState(RunState.RUNNING);

        final Repository repository = repositoryDao.findById(repositoryId);

        repositoryController.storeHarvestStartTime(repository.getId());

        runningInstance = new ListIdentifiers(
                repository.getUrl(),
                repository.getSet(),
                repository.getMetadataPrefix(),
                repository.getDateStamp(),
                httpFetcher,
                responseHandlerFactory,
                oaiDateStamp -> this.storeOaiDateStampAndNewRecords(repository, oaiDateStamp),
                onException,
                oaiRecordHeader ->  recordBatchLoader.addToBatch(repository.getId(), oaiRecordHeader),
                oaiDateStamp -> this.storeOaiDateStampAndNewRecords(repository, oaiDateStamp),
                logMessage -> { /* ignore log message */ }
        );

        runningInstance.harvest();

        runningInstance = null;
        this.setRunState(RunState.WAITING);
    }

    private void storeOaiDateStampAndNewRecords(Repository repository, String oaiDateStamp) {
        try {
            recordBatchLoader.flushBatch(repository.getId());
            repositoryController.storeHarvestDateStamp(repository.getId(), oaiDateStamp);
        } catch (Exception exception) {
            onException.accept(exception);
        }
    }

    void sendInterrupt() {
        if (runningInstance != null) {
            runningInstance.interruptHarvest();
            this.setRunState(RunState.INTERRUPTED);
        } else {
            this.setRunState(RunState.WAITING);
        }
    }

    @JsonProperty
    public RunState getRunState() {
        return runState;
    }

    void setRunState(RunState runState) {
        this.runState = runState;
        stateChangeNotifier.accept(this.runState);
    }
}