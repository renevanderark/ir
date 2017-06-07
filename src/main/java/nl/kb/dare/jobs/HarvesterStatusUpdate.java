package nl.kb.dare.jobs;

import nl.kb.dare.model.SocketUpdate;

public class HarvesterStatusUpdate implements SocketUpdate {

    private final Object data;

    public HarvesterStatusUpdate(Object data) {
        this.data = data;
    }

    @Override
    public String getType() {
        return "harvester-runstate";
    }

    @Override
    public Object getData() {
        return data;
    }
}
