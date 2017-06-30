package nl.kb.dare.model.preproces;

import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class RecordMapper implements ResultSetMapper<Record> {
    @Override
    public Record map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {

        final Timestamp dTsCreate = resultSet.getTimestamp("TS_CREATE");
        final Timestamp dTsProcessed = resultSet.getTimestamp("TS_PROCESSED");

        final String tsCreate = dTsCreate == null ? null : dTsCreate.toString();
        final String tsProcessed = dTsProcessed == null ? null : dTsProcessed.toString();

        return new Record.RecordBuilder()
                .setId(resultSet.getLong("ID"))
                .setState(ProcessStatus.forCode(resultSet.getInt("STATE")))
                .setKbObjId(resultSet.getString("KBOBJID"))
                .setRepositoryId(resultSet.getInt("REPOSITORY_ID"))
                .setOaiIdentifier(resultSet.getString("OAI_ID"))
                .setOaiDateStamp(resultSet.getString("OAI_DATESTAMP"))
                .setTsCreate(tsCreate)
                .setTsProcessed(tsProcessed).createRecord();
    }
}
