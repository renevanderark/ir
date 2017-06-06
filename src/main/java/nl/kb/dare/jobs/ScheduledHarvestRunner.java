package nl.kb.dare.jobs;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.model.RunState;
import nl.kb.dare.model.preproces.RecordBatchLoader;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScheduledHarvestRunner extends AbstractScheduledService {

    private static final Map<Integer, RepositoryHarvester> harvesters = Collections.synchronizedMap(new HashMap<>());
    private final RepositoryController repositoryController;
    private final RecordBatchLoader recordBatchLoader;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final int maxParallel;
    private RepositoryDao repositoryDao;

    public ScheduledHarvestRunner(RepositoryController repositoryController,
                                  RecordBatchLoader recordBatchLoader,
                                  HttpFetcher httpFetcher,
                                  ResponseHandlerFactory responseHandlerFactory,
                                  RepositoryDao repositoryDao, int maxParallel) {

        this.repositoryController = repositoryController;
        this.recordBatchLoader = recordBatchLoader;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.repositoryDao = repositoryDao;
        this.maxParallel = maxParallel;
    }

    public void startHarvest(int repositoryId) {
        if (!harvesters.containsKey(repositoryId)) {
            final RepositoryHarvester harvester = new RepositoryHarvester(
                    repositoryId, repositoryController,
                    recordBatchLoader, httpFetcher, responseHandlerFactory,
                    repositoryDao
            );

            harvesters.put(repositoryId, harvester);
        }


        final RepositoryHarvester repositoryHarvester = harvesters.get(repositoryId);

        if (repositoryHarvester.getRunState() == RunState.WAITING) {
            repositoryHarvester.setRunState(RunState.QUEUED);
        }
    }

    public RunState getHarvesterRunstate(int repositoryId) {
        return harvesters.containsKey(repositoryId) ? harvesters.get(repositoryId).getRunState() : RunState.WAITING;
    }

    public void interruptHarvest(int repositoryId) {
        if (harvesters.containsKey(repositoryId)) {
            harvesters.get(repositoryId).sendInterrupt();
        }
    }


    @Override
    protected void runOneIteration() throws Exception {
        final int slots = (int) (maxParallel - harvesters.entrySet().stream()
                .filter(harvesterEntry -> harvesterEntry.getValue().getRunState() == RunState.RUNNING)
                .count());

        harvesters.entrySet().stream()
                .filter(harvesterEntry -> harvesterEntry.getValue().getRunState() == RunState.QUEUED)
                .limit(slots)
                .map(Map.Entry::getValue)
                .forEach(harvester -> new Thread(harvester).start());
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 200, TimeUnit.MILLISECONDS);
    }
}
