package nl.kb.dare.model.repository;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(RepositoryMapper.class)
public interface RepositoryDao {

    @SqlUpdate("insert into repositories (id, name, url, metadataPrefix, oai_set, datestamp, schedule) " +
            "values (repositories_seq.nextval, :name, :url, :metadataPrefix, :set, :dateStamp, :scheduleCode)")
    void insert(@BindBean Repository repositoryConfig);

    @SqlQuery("select * from repositories where id = :id")
    Repository findById(@Bind("id") int id);

    @SqlUpdate("delete from repositories where id = :id")
    void remove(@Bind("id") int id);

    @SqlUpdate("update repositories " +
            "set name=:r.name, url=:r.url, metadataPrefix=:r.metadataPrefix, oai_set=:r.set, datestamp=:r.dateStamp, schedule=:r.scheduleCode " +
            "where id = :id")
    void update(@Bind("id") Integer id, @BindBean("r") Repository repositoryConfig);

    @SqlQuery("select * from repositories")
    List<Repository> list();

    @SqlUpdate("update repositories " +
            "set enabled = 1 " +
            "where id = :id")
    void enable(@Bind("id") Integer id);

    @SqlUpdate("update repositories " +
            "set enabled = 0 " +
            "where id = :id")
    void disable(@Bind("id") Integer id);

    @SqlUpdate("update repositories set enabled = 0 ")
    void disableAll();

    @SqlUpdate("update repositories set datestamp = :dateStamp where id = :id")
    void setDateStamp(@Bind("id") Integer id, @Bind("dateStamp") String dateStamp);

    @SqlUpdate("update repositories set lastHarvest=CURRENT_TIMESTAMP where id = :id")
    void setLastHarvest(@Bind("id") Integer id);


}
