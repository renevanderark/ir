package nl.kb.dare.model.reporting;

import nl.kb.dare.model.SocketUpdate;

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
