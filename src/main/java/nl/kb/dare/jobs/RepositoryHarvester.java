package nl.kb.dare.jobs;

import nl.kb.dare.model.preproces.RecordBatchLoader;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.oaipmh.ListIdentifiers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RepositoryHarvester implements Runnable {

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
        repositoryController.beforeHarvest(repository.getId());

        runningInstance = new ListIdentifiers(
                repository.getUrl(),
                repository.getSet(),
                repository.getMetadataPrefix(),
                repository.getDateStamp(),
                httpFetcher,
                responseHandlerFactory,
                dateStamp -> {
                    repositoryController.onHarvestComplete(repository.getId(), dateStamp);
                    recordBatchLoader.flushBatch();
                },
                exception -> repositoryController.onHarvestException(repository.getId(), exception),
                oaiRecordHeader -> recordBatchLoader.addToBatch(repository.getId(), oaiRecordHeader),
                dateStamp -> repositoryController.onHarvestProgress(repository.getId(), dateStamp)
        );

        runningInstance.harvest();

        runningInstance = null;
        instances.remove(repository.getId());
    }
}