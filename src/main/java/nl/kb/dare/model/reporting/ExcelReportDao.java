package nl.kb.dare.model.reporting;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

@RegisterMapper(ExcelReportRowMapper.class)
public interface ExcelReportDao {

    @SqlQuery("select STATUS_CODE, TS_CREATE, MESSAGE, URL, OAI_ID, TS_PROCESSED, STATE, KBOBJID, OAI_DATESTAMP " +
            "from error_reports left join dare_preproces on error_reports.dare_preproces_id = dare_preproces.id " +
            "where repository_id = :repositoryId")
    Iterator<ExcelReportRow> getExcelForRepository(@Bind("repositoryId") Integer repositoryId);
}
