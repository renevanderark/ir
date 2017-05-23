package nl.kb.dare.jobs;

import nl.kb.dare.model.SocketUpdate;

public class RecordFetcherUpdate implements SocketUpdate {
    private final ScheduledOaiRecordFetcher.RunState runState;

    public RecordFetcherUpdate(ScheduledOaiRecordFetcher.RunState runState) {
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
