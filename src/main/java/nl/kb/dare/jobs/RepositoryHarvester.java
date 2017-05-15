package nl.kb.dare.jobs;

import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryNotifier;
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
    private final RepositoryNotifier repositoryNotifier;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    private Optional<ListIdentifiers> runningInstance = Optional.empty();

    private RepositoryHarvester(
            Repository repository, RepositoryNotifier repostoryNotifier,
            HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {

        this.repository = repository;
        this.repositoryNotifier = repostoryNotifier;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }


    public static Optional<ListIdentifiers> getRunningInstance(Integer repositoryId) {
        if (instances.containsKey(repositoryId)) {
            final RepositoryHarvester instance = instances.get(repositoryId);
            return instance.runningInstance;
        }

        return Optional.empty();
    }

    public static RepositoryHarvester getInstance(
            Repository repository,
            RepositoryNotifier repositoryNotifier,
            HttpFetcher httpFetcher,
            ResponseHandlerFactory responseHandlerFactory) {

        if (instances.containsKey(repository.getId())) {
            return instances.get(repository.getId());
        }

        final RepositoryHarvester newInstance = new RepositoryHarvester(
                repository, repositoryNotifier, httpFetcher, responseHandlerFactory);

        instances.put(repository.getId(), newInstance);

        return newInstance;
    }

    @Override
    public void run() {
        repositoryNotifier.beforeHarvest(repository.getId());

        runningInstance = Optional.of(new ListIdentifiers(
                repository.getUrl(),
                repository.getSet(),
                repository.getMetadataPrefix(),
                repository.getDateStamp(),
                httpFetcher,
                responseHandlerFactory,
                dateStamp -> repositoryNotifier.onHarvestComplete(repository.getId(), dateStamp),
                exception -> repositoryNotifier.onHarvestException(repository.getId(), exception),
                oaiRecordHeader -> repositoryNotifier.onOaiRecord(repository.getId(), oaiRecordHeader),
                dateStamp -> repositoryNotifier.onHarvestProgress(repository.getId(), dateStamp)
        ));

        runningInstance.get().harvest();

        runningInstance = Optional.empty();
        instances.remove(repository.getId());
    }
}