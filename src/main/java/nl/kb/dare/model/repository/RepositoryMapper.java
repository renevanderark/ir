package nl.kb.dare.model.repository;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RepositoryMapper implements ResultSetMapper<Repository> {


    @Override
    public Repository map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        final Date lastHarvest = resultSet.getDate("lastHarvest");

        return new Repository.RepositoryBuilder()
                .setUrl(resultSet.getString("url"))
                .setName(resultSet.getString("name"))
                .setMetadataPrefix(resultSet.getString("metadataPrefix"))
                .setSet(resultSet.getString("oai_set"))
                .setDateStamp(resultSet.getString("dateStamp"))
                .setEnabled(resultSet.getBoolean("enabled"))
                .setSchedule(HarvestSchedule.forCode(resultSet.getInt("schedule")))
                .setId(resultSet.getInt("id"))
                .setLastHarvest(lastHarvest == null ? null : lastHarvest.toLocalDate())
                .createRepository();
    }
}
