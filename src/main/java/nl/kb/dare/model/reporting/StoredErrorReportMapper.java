package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StoredErrorReportMapper implements ResultSetMapper<StoredErrorReport> {

    @Override
    public StoredErrorReport map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {

        return new StoredErrorReport(
                resultSet.getString("KBOBJID"),
                resultSet.getString("OAI_ID"),
                resultSet.getString("URL"),
                resultSet.getString("MESSAGE"),
                resultSet.getInt("STATUS_CODE"),
                resultSet.getString("NAME"),
                resultSet.getDate("TS_CREATE")
        );
    }
}
