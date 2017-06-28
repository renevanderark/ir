package nl.kb.dare.websocket.socketupdate;

import nl.kb.dare.websocket.SocketUpdate;

public class RecordStatusUpdate implements SocketUpdate {

    private final Object data;

    public RecordStatusUpdate(Object data) {
        this.data = data;
    }

    @Override
    public String getType() {
        return "record-change";
    }

    @Override
    public Object getData() {
        return data;
    }
}
