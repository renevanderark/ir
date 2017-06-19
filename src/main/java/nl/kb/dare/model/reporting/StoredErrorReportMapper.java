package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StoredErrorReportMapper implements ResultSetMapper<StoredErrorReport> {

    @Override
    public StoredErrorReport map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
        final String message = resultSet.getString("MESSAGE");
        final String url = resultSet.getString("URL");
        final int statusCode = resultSet.getInt("STATUS_CODE");

        final Clob cStackTrace = resultSet.getClob("STACKTRACE");

        final String stackTrace = cStackTrace == null ? "" :
                cStackTrace.getSubString(1L, (int) cStackTrace.length());

        return new StoredErrorReport(statusCode, message, url, stackTrace);
    }
}
