package nl.kb.dare.model.preproces;

import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.websocket.socketupdate.RecordStatusUpdate;
import org.skife.jdbi.v2.DBI;

import static nl.kb.dare.model.Aggregations.getAggregateCounts;

public class RecordReporter {

    private static final String SQL = "select count(*) as count," +
            "dare_preproces.state as status_code, " +
            "dare_preproces.repository_id as repository_id " +
            "from dare_preproces " +
            "group by repository_id, state";

    private final DBI db;

    public RecordReporter(DBI db) {
        this.db = db;
    }

    public RecordStatusUpdate getStatusUpdate() {
        return new RecordStatusUpdate(getAggregateCounts(db, SQL, this::codeToHumanKey));
    }

    private String codeToHumanKey(int statusCode) {
        final ProcessStatus processStatus = ProcessStatus.forCode(statusCode);
        if (processStatus == null) {
            return Integer.toString(statusCode);
        }
        return processStatus.getStatus();
    }
}
