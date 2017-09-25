package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ExcelReportRowMapper implements ResultSetMapper<ExcelReportRow> {

    @Override
    public ExcelReportRow map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {

        final Timestamp dTsCreate = resultSet.getTimestamp("TS_CREATE");
        final Timestamp dTsProcessed = resultSet.getTimestamp("TS_PROCESSED");

        final String tsCreate = dTsCreate == null ? null : dTsCreate.toString();
        final String tsProcessed = dTsProcessed == null ? null : dTsProcessed.toString();

        return new ExcelReportRow.ExcelReportRowBuilder()
                .setIpName(resultSet.getString("IP_NAME"))
                .setMessage(resultSet.getString("MESSAGE"))
                .setOaiDatestamp(resultSet.getString("OAI_DATESTAMP"))
                .setState(resultSet.getInt("STATE"))
                .setStatusCode(resultSet.getInt("STATUS_CODE"))
                .setOaiId(resultSet.getString("OAI_ID"))
                .setUrl(resultSet.getString("URL"))
                .setTsCreate(tsCreate)
                .setTsProcessed(tsProcessed)
                .createExcelReportRow();
    }
}
