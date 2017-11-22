package nl.kb.dare.scheduledjobs;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.objectharvester.ObjectHarvester;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.dare.websocket.socketupdate.ObjectHarvesterRunstateUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectHarvestSchedulerDaemon extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHarvestSchedulerDaemon.class);

    private static AtomicInteger runningWorkers = new AtomicInteger(0);

    private final SocketNotifier socketNotifier;
    private final Integer maxParallelDownloads;
    private final Long downloadQueueFillDelayMs;
    private ObjectHarvester objectHarvester;

    public RunState getRunState() {
        return runState;
    }

    public enum RunState {
        RUNNING, DISABLING, DISABLED
    }

    private RunState runState;

    public ObjectHarvestSchedulerDaemon(ObjectHarvester objectHarvester,
                                        SocketNotifier socketNotifier,
                                        Integer maxWorkers,
                                        Long downloadQueueFillDelayMs) {

        this.socketNotifier = socketNotifier;
        this.maxParallelDownloads = maxWorkers;
        this.downloadQueueFillDelayMs = downloadQueueFillDelayMs;
        this.runState = RunState.DISABLED;
        this.objectHarvester = objectHarvester;

    }

    @Override
    protected void runOneIteration() throws Exception {
        try {
            if (runState == RunState.DISABLED || runState == RunState.DISABLING) {
                checkRunState();
                return;
            }

            final List<Thread> workers = objectHarvester.harvestNextPublications(maxParallelDownloads, runningWorkers);

            if (runState == RunState.DISABLING) {
                for (Thread worker : workers) {
                    worker.join();
                }
            }

            checkRunState();
        } catch (Exception e) {
            LOG.error("Failed to fetch pending records from database", e);
        }
    }

    private void checkRunState() {
        final RunState runStateBefore = runState;
        if (runState == RunState.DISABLED || runState == RunState.DISABLING){
            runState = runningWorkers.get() > 0 ? RunState.DISABLING : RunState.DISABLED;
        } else {
            runState = RunState.RUNNING;
        }

        if (runStateBefore != runState) {
            socketNotifier.notifyUpdate(new ObjectHarvesterRunstateUpdate(runState));
        }
    }

    public void enable() {
        runState = RunState.RUNNING;
        socketNotifier.notifyUpdate(new ObjectHarvesterRunstateUpdate(runState));
    }

    public void disable() {
        runState = RunState.DISABLING;
        socketNotifier.notifyUpdate(new ObjectHarvesterRunstateUpdate(runState));
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, downloadQueueFillDelayMs, TimeUnit.MILLISECONDS);
    }
}
