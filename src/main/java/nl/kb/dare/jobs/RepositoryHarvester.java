package nl.kb.dare.jobs;

import nl.kb.dare.model.preproces.RecordBatchLoader;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.oaipmh.ListIdentifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RepositoryHarvester implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryHarvester.class);

    private static final Map<Integer, RepositoryHarvester> instances = Collections.synchronizedMap(new HashMap<>());
    private final Repository repository;
    private final RepositoryController repositoryController;
    private final RecordBatchLoader recordBatchLoader;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    private ListIdentifiers runningInstance = null;

    private RepositoryHarvester(
            Repository repository,
            RepositoryController repositoryController,
            RecordBatchLoader recordBatchLoader,
            HttpFetcher httpFetcher,
            ResponseHandlerFactory responseHandlerFactory) {

        this.repository = repository;
        this.repositoryController = repositoryController;
        this.recordBatchLoader = recordBatchLoader;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }


    public static Optional<ListIdentifiers> getRunningInstance(Integer repositoryId) {
        if (instances.containsKey(repositoryId)) {
            final RepositoryHarvester instance = instances.get(repositoryId);
            return Optional.of(instance.runningInstance);
        }

        return Optional.empty();
    }

    public static RepositoryHarvester getInstance(
            Repository repository,
            RepositoryController repositoryController,
            RecordBatchLoader recordBatchLoader,
            HttpFetcher httpFetcher,
            ResponseHandlerFactory responseHandlerFactory) {

        if (instances.containsKey(repository.getId())) {
            return instances.get(repository.getId());
        }

        final RepositoryHarvester newInstance = new RepositoryHarvester(
                repository, repositoryController, recordBatchLoader, httpFetcher, responseHandlerFactory);

        instances.put(repository.getId(), newInstance);

        return newInstance;
    }

    @Override
    public void run() {
        LOG.info("Staring harvest thread, running: " + instances.keySet().size());
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
        instances.remove(repository.getId());
        LOG.info("Ended harvest thread, running: " + instances.keySet().size());
    }
}