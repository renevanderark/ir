package nl.kb.dare.model.repository.oracle;

import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryMapper;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.math.BigDecimal;

@RegisterMapper(RepositoryMapper.class)
public abstract class OracleRepositoryDao implements RepositoryDao {


    @SqlUpdate("insert into repositories (id, name, url, metadataPrefix, oai_set, datestamp) " +
            "values (repositories_seq.nextval, :name, :url, :metadataPrefix, :set, :dateStamp)")
    abstract void oracleInsert(@BindBean Repository repositoryConfig);

    @SqlQuery("select repositories_seq.currval from dual")
    abstract BigDecimal getId();

    public Integer insert(Repository repositoryConfig) {
        oracleInsert(repositoryConfig);

        return getId().intValue();
    }
}
