package nl.kb.dare.model.preproces;

import nl.kb.oaipmh.OaiRecordHeader;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import java.util.List;

public interface RecordDao {

    @SqlBatch("INSERT INTO DARE_PREPROCES (ID, STATE, KBOBJID, TS_CREATE, FINGERPRINT, REPOSITORY_ID) " +
            "VALUES (SEQ_DARE_PREPROCES.nextval, :state, :kbObjId, CURRENT_TIMESTAMP, :fingerprint, :repositoryId)")
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
}
