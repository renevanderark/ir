package nl.kb.dare.model;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public class Aggregations {

    public static Map<String, Map<String, Object>> getAggregateCounts(DBI db, String sql, IntFunction<String> getCodeKey) {
        final Map<String, Map<String, Object>> result = new HashMap<>();

        final Handle handle = db.open();
        for (Map<String, Object> row : handle.createQuery(sql)) {
            final String repositoryId = String.format("%d", getRowInt(row, "repository_id"));
            final Integer statusCode = getRowInt(row, "status_code");
            final Map<String, Object> statusMap = result.getOrDefault(repositoryId, new HashMap<>());
            statusMap.put(getCodeKey.apply(statusCode), row.get("count"));
            result.put(repositoryId, statusMap);
        }
        handle.close();
        return result;
    }

    private static Integer getRowInt(Map<String, Object> row, String repository_id) {
        return ((BigDecimal) row.get(repository_id)).intValue();
    }
}
