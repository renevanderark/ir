package nl.kb.dare.websocket.socketupdate;

import nl.kb.dare.scheduledjobs.ObjectHarvesterDaemon;
import nl.kb.dare.websocket.SocketUpdate;

public class RecordFetcherUpdate implements SocketUpdate {
    private final ObjectHarvesterDaemon.RunState runState;

    public RecordFetcherUpdate(ObjectHarvesterDaemon.RunState runState) {
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
