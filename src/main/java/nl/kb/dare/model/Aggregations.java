package nl.kb.dare.model;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public class Aggregations {
    private Aggregations() {

    }

    public static Map<String, Map<String, Object>> getAggregateCounts(DBI db, String sql, IntFunction<String> getCodeKey) {
        final Map<String, Map<String, Object>> result = new HashMap<>();

        final Handle handle = db.open();
        for (Map<String, Object> row : handle.createQuery(sql)) {
            final String repositoryId = String.format("%d", getRowInt(row, "repository_id"));
            final Integer statusCode = getRowInt(row, "status_code");
            final Map<String, Object> statusMap = result.getOrDefault(repositoryId, new HashMap<>());
            final String codeKey = getCodeKey.apply(statusCode);
            if (statusMap.containsKey(codeKey)) {
                statusMap.put(codeKey, getRowInt(row, "count") +
                        ((BigDecimal) statusMap.get(codeKey)).intValue());
            } else {
                statusMap.put(codeKey, row.get("count"));
            }


            result.put(repositoryId, statusMap);
        }
        handle.close();
        return result;
    }

    private static Integer getRowInt(Map<String, Object> row, String repositoryId) {
        final Object rowInt = row.get(repositoryId);
        if (rowInt == null) { return 0; }
        return ((BigDecimal) rowInt).intValue();
    }
}
