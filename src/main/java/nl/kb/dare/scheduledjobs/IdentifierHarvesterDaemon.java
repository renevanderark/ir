package nl.kb.dare.scheduledjobs;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.model.RunState;
import nl.kb.dare.model.preproces.RecordBatchLoader;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.dare.websocket.socketupdate.HarvesterStatusUpdate;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IdentifierHarvesterDaemon extends AbstractScheduledService {
    private static final Map<Integer, IdentifierHarvester> harvesters = Collections.synchronizedMap(new HashMap<>());
    private final RepositoryController repositoryController;
    private final RecordBatchLoader recordBatchLoader;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final SocketNotifier socketNotifier;
    private final IdentifierHarvesterErrorFlowHandler errorFlowHandler;
    private final int maxParallel;
    private RepositoryDao repositoryDao;

    public IdentifierHarvesterDaemon(RepositoryController repositoryController,
                                     RecordBatchLoader recordBatchLoader,
                                     HttpFetcher httpFetcher,
                                     ResponseHandlerFactory responseHandlerFactory,
                                     RepositoryDao repositoryDao,
                                     SocketNotifier socketNotifier,
                                     int maxParallel) {

        this.repositoryController = repositoryController;
        this.recordBatchLoader = recordBatchLoader;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.repositoryDao = repositoryDao;
        this.socketNotifier = socketNotifier;
        this.maxParallel = maxParallel;
        this.errorFlowHandler = new IdentifierHarvesterErrorFlowHandler(repositoryController, this);
    }

    public void startHarvest(int repositoryId) {
        if (!harvesters.containsKey(repositoryId)) {
            final IdentifierHarvester harvester = new IdentifierHarvester(
                    repositoryId, repositoryController,
                    recordBatchLoader, httpFetcher, responseHandlerFactory,
                    repositoryDao, (RunState runState) -> notifyStateChange(),
                    errorFlowHandler::handlerIdentifierHarvestException
            );

            harvesters.put(repositoryId, harvester);
        }


        final IdentifierHarvester repositoryHarvester = harvesters.get(repositoryId);

        if (repositoryHarvester.getRunState() == RunState.WAITING) {
            repositoryHarvester.setRunState(RunState.QUEUED);
        }
    }

    void interruptAllHarvests() {
        for (Map.Entry<Integer, IdentifierHarvester> entry : harvesters.entrySet()) {
            entry.getValue().sendInterrupt();
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

    public HarvesterStatusUpdate getStatusUpdate() {
        return new HarvesterStatusUpdate(harvesters);
    }

    private void notifyStateChange() {
        socketNotifier.notifyUpdate(getStatusUpdate());
    }

    @Override
    protected Scheduler scheduler() {

        return Scheduler.newFixedRateSchedule(0, 200, TimeUnit.MILLISECONDS);
    }
}
