package nl.kb.dare.scheduledjobs;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.identifierharvester.IdentifierHarvestErrorFlowHandler;
import nl.kb.dare.identifierharvester.IdentifierHarvester;
import nl.kb.dare.model.RunState;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.dare.websocket.socketupdate.HarvesterStatusUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IdentifierHarvestSchedulerDaemon extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(IdentifierHarvestSchedulerDaemon.class);

    private static final Map<Integer, IdentifierHarvester> harvesters = Collections.synchronizedMap(new HashMap<>());
    private final SocketNotifier socketNotifier;
    private final IdentifierHarvestErrorFlowHandler errorFlowHandler;
    private final int maxParallel;
    private final IdentifierHarvester.Builder harvesterBuilder;

    public IdentifierHarvestSchedulerDaemon(
            IdentifierHarvester.Builder harvesterBuilder,
            SocketNotifier socketNotifier,
            IdentifierHarvestErrorFlowHandler errorFlowHandler,
            int maxParallel
        ) {

        this.socketNotifier = socketNotifier;
        this.maxParallel = maxParallel;
        this.errorFlowHandler = errorFlowHandler;
        this.harvesterBuilder = harvesterBuilder;
    }

    public void startHarvest(int repositoryId) {
        if (!harvesters.containsKey(repositoryId)) {
            final IdentifierHarvester harvester = harvesterBuilder
                    .setRepositoryId(repositoryId)
                    .setStateChangeNotifier((RunState runState) -> notifyStateChange())
                    .setOnException(this::handleException)
                    .createIdentifierHarvester();

            harvesters.put(repositoryId, harvester);
        }


        final IdentifierHarvester repositoryHarvester = harvesters.get(repositoryId);

        if (repositoryHarvester.getRunState() == RunState.WAITING) {
            repositoryHarvester.setRunState(RunState.QUEUED);
        }
    }

    private void handleException(Exception ex) {
        interruptAllHarvests();
        errorFlowHandler.handleIdentifierHarvestException(ex);
    }

    private void interruptAllHarvests() {
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
        try {
            final int slots = (int) (maxParallel - harvesters.entrySet().stream()
                    .filter(harvesterEntry -> harvesterEntry.getValue().getRunState() == RunState.RUNNING)
                    .count());

            harvesters.entrySet().stream()
                    .filter(harvesterEntry -> harvesterEntry.getValue().getRunState() == RunState.QUEUED)
                    .limit(slots)
                    .map(Map.Entry::getValue)
                    .forEach(harvester -> new Thread(harvester).start());
        } catch (Exception e) {
            LOG.error("Failed to start identifier harvesters", e);
        }
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
