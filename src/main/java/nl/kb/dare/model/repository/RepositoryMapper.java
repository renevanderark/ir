package nl.kb.dare.model.repository;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RepositoryMapper implements ResultSetMapper<Repository> {


    @Override
    public Repository map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        final String url = resultSet.getString("url");
        final String name = resultSet.getString("name");
        final String metadataPrefix = resultSet.getString("metadataPrefix");
        final String set = resultSet.getString("oai_set");
        final String dateStamp = resultSet.getString("dateStamp");
        final Integer id = resultSet.getInt("id");
        final Boolean enabled = resultSet.getBoolean("enabled");
        final HarvestSchedule schedule = HarvestSchedule.forCode(resultSet.getInt("schedule"));
        return new Repository(url, name, metadataPrefix, set, dateStamp, enabled, schedule, id);
    }
}
