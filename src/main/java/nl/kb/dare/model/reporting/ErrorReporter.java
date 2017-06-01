package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import static nl.kb.dare.model.Aggregations.getAggregateCounts;

public class ErrorReporter {
    private static final String STATUS_UPDATE_SQL =
            "select count(*) as count, status_code as status_code, repository_id as repository_id " +
            "from error_reports " +
            "left join dare_preproces on error_reports.dare_preproces_id = dare_preproces.id " +
            "group by status_code, repository_id";

    private static final String REPORT_SQL =
            "            select error_reports.*, kbobjid, repositories.name, oai_id from error_reports\n" +
                    "            left join dare_preproces on error_reports.dare_preproces_id = dare_preproces.id\n" +
                    "            left join repositories on dare_preproces.repository_id = repositories.id";

    private final DBI db;

    public ErrorReporter(DBI db) {
        this.db = db;
    }
    
    public ErrorStatusUpdate getStatusUpdate() {
        return new ErrorStatusUpdate(getAggregateCounts(db, STATUS_UPDATE_SQL, this::codeToHumanKey));

    }

    public void getReport(OutputStream out) {
        try(final Handle h = db.open()) {
            new ExcelReport(h.createQuery(REPORT_SQL).map(new StoredErrorReportMapper()).iterator()).build(out);
        } catch (SQLException e) {

        } catch (IOException e) {

        }
    }

    private String codeToHumanKey(int statusCode) {
        return "" + statusCode;
    }
}
