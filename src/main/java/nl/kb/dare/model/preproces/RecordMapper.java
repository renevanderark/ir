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

        final Long id = resultSet.getLong("ID");
        final Integer repositoryId = resultSet.getInt("REPOSITORY_ID");
        final Integer state = resultSet.getInt("STATE");
        final String kbObjId = resultSet.getString("KBOBJID");
        final String oaiIdentifier = resultSet.getString("OAI_ID");
        final String oaiDateStamp = resultSet.getString("OAI_DATESTAMP");
        final Timestamp dTsCreate = resultSet.getTimestamp("TS_CREATE");
        final Timestamp dTsProcessed = resultSet.getTimestamp("TS_PROCESSED");

        final String tsCreate = dTsCreate == null ? null : dTsCreate.toString();
        final String tsProcessed = dTsProcessed == null ? null : dTsProcessed.toString();

        return new Record(id, ProcessStatus.forCode(state), kbObjId, repositoryId, oaiIdentifier, oaiDateStamp,
                tsCreate, tsProcessed);
    }
}
