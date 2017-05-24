package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.DBI;

import static nl.kb.dare.model.Aggregations.getAggregateCounts;

public class ErrorReporter {
    private static final String SQL =
            "select count(*) as count, status_code as status_code, repository_id as repository_id " +
            "from error_reports " +
            "left join dare_preproces on error_reports.dare_preproces_id = dare_preproces.id\n" +
            "group by status_code, repository_id";

    private final DBI db;

    public ErrorReporter(DBI db) {
        this.db = db;
    }
    
    public ErrorStatusUpdate getStatusUpdate() {
        return new ErrorStatusUpdate(getAggregateCounts(db, SQL, this::codeToHumanKey));

    }

    private String codeToHumanKey(int statusCode) {
        return "" + statusCode;
    }
}
