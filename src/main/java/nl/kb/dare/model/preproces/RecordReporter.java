package nl.kb.dare.model.preproces;

import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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

    RecordStatusUpdate getStatusUpdate() {
        final Map<String, Map<String, Object>> result = new HashMap<>();

        final Handle handle = db.open();
        for (Map<String, Object> row : handle.createQuery(SQL)) {
            final String repositoryId = String.format("%d", getRowInt(row, "repository_id"));
            final Integer statusCode = getRowInt(row, "status_code");
            final Map<String, Object> statusMap = result.getOrDefault(repositoryId, new HashMap<>());
            final ProcessStatus processStatus = ProcessStatus.forCode(statusCode);
            if (processStatus != null) {
                statusMap.put(processStatus.getStatus(), row.get("count"));
            }
            result.put(repositoryId, statusMap);
        }
        handle.close();
        return new RecordStatusUpdate(result);
    }


    private Integer getRowInt(Map<String, Object> row, String repository_id) {
        return ((BigDecimal) row.get(repository_id)).intValue();
    }
}
