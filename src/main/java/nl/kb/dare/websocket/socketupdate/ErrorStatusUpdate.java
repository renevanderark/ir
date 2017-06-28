package nl.kb.dare.websocket.socketupdate;

import nl.kb.dare.websocket.SocketUpdate;

public class ErrorStatusUpdate implements SocketUpdate {

    private final Object data;

    public ErrorStatusUpdate(Object data) {

        this.data = data;
    }

    @Override
    public String getType() {
        return "error-change";
    }

    @Override
    public Object getData() {
        return data;
    }
}
