package nl.kb.dare.websocket.socketupdate;

import nl.kb.dare.scheduledjobs.ObjectHarvestSchedulerDaemon;
import nl.kb.dare.websocket.SocketUpdate;

public class ObjectHarvesterRunstateUpdate implements SocketUpdate {
    private final ObjectHarvestSchedulerDaemon.RunState runState;

    public ObjectHarvesterRunstateUpdate(ObjectHarvestSchedulerDaemon.RunState runState) {
        this.runState = runState;
    }

    @Override
    public String getType() {
        return "record-fetcher";
    }

    @Override
    public Object getData() {
        return runState;
    }
}
