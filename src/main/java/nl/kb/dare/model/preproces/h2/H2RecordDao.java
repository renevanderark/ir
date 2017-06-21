package nl.kb.dare.model.preproces.h2;

import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordMapper;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(RecordMapper.class)
public abstract class H2RecordDao implements RecordDao {

    @SqlBatch("INSERT INTO DARE_PREPROCES (STATE, KBOBJID, TS_CREATE, REPOSITORY_ID, OAI_ID, OAI_DATESTAMP, LOOKUP) " +
            "VALUES (:state, :kbObjId, CURRENT_TIMESTAMP, :repositoryId, :oaiIdentifier, :oaiDateStamp, :kbObjId || :oaiIdentifier)")
    public abstract void insertBatch(@BindBean List<Record> recordList);
}
