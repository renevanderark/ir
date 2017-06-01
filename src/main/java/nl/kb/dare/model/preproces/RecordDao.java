package nl.kb.dare.model.preproces;

import nl.kb.oaipmh.OaiRecordHeader;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(RecordMapper.class)
public interface RecordDao {

    @SqlBatch("INSERT INTO DARE_PREPROCES (ID, STATE, KBOBJID, TS_CREATE, FINGERPRINT, REPOSITORY_ID, OAI_ID) " +
            "VALUES (SEQ_DARE_PREPROCES.nextval, :state, :kbObjId, CURRENT_TIMESTAMP, :fingerprint, :repositoryId, :oaiIdentifier)")
    void insertBatch(@BindBean List<Record> recordList);

    @SqlQuery("select case " +
            "            when exists (select 1 " +
            "                         from dare_preproces " +
            "                         where fingerprint = :fingerprint) " +
            "            then 1 " +
            "            else 0 " +
            "        end " +
            "from dual")
    Boolean existsByFingerPrint(@BindBean OaiRecordHeader oaiRecordHeader);



    @SqlQuery("select * from DARE_PREPROCES where STATE = :process_status_code AND REPOSITORY_ID = :repository_id AND ROWNUM <= :limit")
    List<Record> fetchNextWithProcessStatusByRepositoryId(
            @Bind("process_status_code") Integer processStatusCode,
            @Bind("limit") Integer limit,
            @Bind("repository_id") Integer repositoryId);



    @SqlUpdate("update DARE_PREPROCES set STATE = :state where ID = :id")
    void updateState(@BindBean Record record);

    @SqlQuery("select * from DARE_PREPROCES where STATE = :process_status_code")
    List<Record> fetchAllByProcessStatus(@Bind("process_status_code") Integer processStatusCode);
}
