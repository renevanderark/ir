package nl.kb.dare.identifierharvester;

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

    private IdentifierHarvester(Builder identifierHarvesterBuilder) {

        this.repositoryId = identifierHarvesterBuilder.repositoryId;
        this.repositoryController = identifierHarvesterBuilder.repositoryController;
        this.recordBatchLoader = identifierHarvesterBuilder.recordBatchLoader;
        this.httpFetcher = identifierHarvesterBuilder.httpFetcher;
        this.responseHandlerFactory = identifierHarvesterBuilder.responseHandlerFactory;
        this.repositoryDao = identifierHarvesterBuilder.repositoryDao;
        this.stateChangeNotifier = identifierHarvesterBuilder.stateChangeNotifier;
        this.onException = identifierHarvesterBuilder.onException;
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

    public void sendInterrupt() {
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

    public void setRunState(RunState runState) {
        this.runState = runState;
        stateChangeNotifier.accept(this.runState);
    }

    public static class Builder {
        private final RepositoryController repositoryController;
        private final RecordBatchLoader recordBatchLoader;
        private final HttpFetcher httpFetcher;
        private final ResponseHandlerFactory responseHandlerFactory;
        private final RepositoryDao repositoryDao;
        private Integer repositoryId;
        private Consumer<RunState> stateChangeNotifier;
        private Consumer<Exception> onException;

        public Builder(RepositoryController repositoryController,
                       RecordBatchLoader recordBatchLoader,
                       HttpFetcher httpFetcher,
                       ResponseHandlerFactory responseHandlerFactory,
                       RepositoryDao repositoryDao) {

            this.repositoryController = repositoryController;
            this.recordBatchLoader = recordBatchLoader;
            this.httpFetcher = httpFetcher;
            this.responseHandlerFactory = responseHandlerFactory;
            this.repositoryDao = repositoryDao;
        }

        public Builder setRepositoryId(Integer repositoryId) {
            this.repositoryId = repositoryId;
            return this;
        }


        public Builder setStateChangeNotifier(Consumer<RunState> stateChangeNotifier) {
            this.stateChangeNotifier = stateChangeNotifier;
            return this;
        }

        public Builder setOnException(Consumer<Exception> onException) {
            this.onException = onException;
            return this;
        }

        public IdentifierHarvester createIdentifierHarvester() {
            return new IdentifierHarvester(this);
        }
    }
}