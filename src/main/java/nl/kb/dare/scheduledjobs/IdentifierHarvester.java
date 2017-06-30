package nl.kb.dare.scheduledjobs;

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

public class IdentifierHarvester implements Runnable {
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

    IdentifierHarvester(
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

        runningInstance = new ListIdentifiers.ListIdentifiersBuilder()
                .setOaiUrl(repository.getUrl())
                .setOaiSet(repository.getSet())
                .setOaiMetadataPrefix(repository.getMetadataPrefix())
                .setOaiDatestamp(repository.getDateStamp())
                .setHttpFetcher(httpFetcher)
                .setResponseHandlerFactory(responseHandlerFactory)
                .setOnHarvestComplete(oaiDateStamp -> this.storeOaiDateStampAndNewRecords(repository, oaiDateStamp))
                .setOnException(exception -> this.onHarvestException(repository, exception))
                .setOnOaiRecordHeader(oaiRecordHeader -> recordBatchLoader.addToBatch(repository.getId(), oaiRecordHeader))
                .setOnProgress(oaiDateStamp -> this.storeOaiDateStampAndNewRecords(repository, oaiDateStamp))
                .createListIdentifiers();

        runningInstance.harvest();

        runningInstance = null;
        this.setRunState(RunState.WAITING);
    }

    private void onHarvestException(Repository repository, Exception exception) {
        final String message = String.format("Failed to fetch batch from OAI endpoint %s", repository.getUrl());
        onException.accept(new IdentifierHarvesterException(message, exception));
    }

    private void storeOaiDateStampAndNewRecords(Repository repository, String oaiDateStamp) {
        try {
            recordBatchLoader.flushBatch(repository.getId());
            repositoryController.storeHarvestDateStamp(repository.getId(), oaiDateStamp);
        } catch (Exception exception) {
            onException.accept(
                new IdentifierHarvesterException("Failed to generate numbers with numbers endpoint", exception)
            );
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