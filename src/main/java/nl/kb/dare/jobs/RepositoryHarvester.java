package nl.kb.dare.jobs;

import nl.kb.dare.model.RunState;
import nl.kb.dare.model.preproces.RecordBatchLoader;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.oaipmh.ListIdentifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryHarvester implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryHarvester.class);

    private final Integer repositoryId;
    private final RepositoryController repositoryController;
    private final RecordBatchLoader recordBatchLoader;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final RepositoryDao repositoryDao;

    private ListIdentifiers runningInstance = null;
    private RunState runState = RunState.WAITING;

    RepositoryHarvester(
            Integer repositoryId,
            RepositoryController repositoryController,
            RecordBatchLoader recordBatchLoader,
            HttpFetcher httpFetcher,
            ResponseHandlerFactory responseHandlerFactory,
            RepositoryDao repositoryDao) {

        this.repositoryId = repositoryId;
        this.repositoryController = repositoryController;
        this.recordBatchLoader = recordBatchLoader;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.repositoryDao = repositoryDao;
    }

    @Override
    public void run() {
        this.runState = RunState.RUNNING;
        final Repository repository = repositoryDao.findById(repositoryId);

        repositoryController.beforeHarvest(repository.getId());

        runningInstance = new ListIdentifiers(
                repository.getUrl(),
                repository.getSet(),
                repository.getMetadataPrefix(),
                repository.getDateStamp(),
                httpFetcher,
                responseHandlerFactory,
                dateStamp -> { // onHarvestComplete
                    repositoryController.onHarvestComplete(repository.getId(), dateStamp);
                    recordBatchLoader.flushBatch(repository.getId());
                },
                exception -> repositoryController.onHarvestException(repository.getId(), exception),
                oaiRecordHeader -> { // onRecord
                    recordBatchLoader.addToBatch(repository.getId(), oaiRecordHeader);
                },
                dateStamp -> { // onProgress
                    repositoryController.onHarvestProgress(repository.getId(), dateStamp);
                    recordBatchLoader.flushBatch(repository.getId());
                }, (String logMessage) -> { // onLogMessage
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("{} {}", repository.getName(), logMessage);
                    }
                }
        );

        runningInstance.harvest();

        runningInstance = null;
        this.runState = RunState.WAITING;
    }

    void sendInterrupt() {
        if (runningInstance != null) {
            runningInstance.interruptHarvest();
            this.runState = RunState.INTERRUPTED;
        } else {
            this.runState = RunState.WAITING;
        }
    }

    RunState getRunState() {
        return runState;
    }

    void setRunState(RunState runState) {
        this.runState = runState;
    }
}