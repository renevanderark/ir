package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface ErrorReportDao {

    @SqlUpdate(
        "INSERT INTO ERROR_REPORTS (DARE_PREPROCES_ID, MESSAGE, URL, STACKTRACE, STATUS_CODE, TS_CREATE) " +
        "VALUES (:recordId, :report.errorMessage, :report.url, :report.filteredStackTrace, :report.statusCode, CURRENT_TIMESTAMP)"
    )
    void insert(@Bind("recordId") Integer recordId, @BindBean("report") ErrorReport errorReport);
}
