package nl.kb.dare.model.preproces;

import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RecordMapper implements ResultSetMapper<Record> {
    @Override
    public Record map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {

        final Integer id = resultSet.getInt("ID");
        final Integer repositoryId = resultSet.getInt("REPOSITORY_ID");
        final Integer state = resultSet.getInt("STATE");
        final String kbObjId = resultSet.getString("KBOBJID");
        final String fingerprint = resultSet.getString("FINGERPRINT");
        final String oaiIdentifier = resultSet.getString("OAI_ID");

        return new Record(id, ProcessStatus.forCode(state), kbObjId, fingerprint, repositoryId, oaiIdentifier);
    }
}
