package nl.kb.dare.model.preproces;

import nl.kb.dare.model.SocketUpdate;

class RecordStatusUpdate implements SocketUpdate {

    private final Object data;

    RecordStatusUpdate(Object data) {
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
