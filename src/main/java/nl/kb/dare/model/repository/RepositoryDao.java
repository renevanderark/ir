package nl.kb.dare.model.repository;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(RepositoryMapper.class)
public interface RepositoryDao {

    @SqlUpdate("insert into repositories (name, url, metadataPrefix, oai_set, datestamp) " +
            "values (:name, :url, :metadataPrefix, :set, :dateStamp)")
    @GetGeneratedKeys
    Integer insert(@BindBean Repository resvnpositoryConfig);

    @SqlQuery("select * from repositories where id = :id")
    Repository findById(@Bind("id") int id);

    @SqlUpdate("delete from repositories where id = :id")
    void remove(@Bind("id") int id);

    @SqlUpdate("update repositories " +
            "set name=:r.name, url=:r.url, metadataPrefix=:r.metadataPrefix, oai_set=:r.set, datestamp=:r.dateStamp " +
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
}
