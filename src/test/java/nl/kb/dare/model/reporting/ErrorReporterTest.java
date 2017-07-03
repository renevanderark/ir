package nl.kb.dare.model.reporting;

import nl.kb.dare.databasetasks.SchemaLoader;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordUtil;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.websocket.socketupdate.ErrorStatusUpdate;
import nl.kb.oaipmh.OaiStatus;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class ErrorReporterTest {

    private JdbcConnectionPool dataSource;
    private Handle handle;
    private ErrorReportDao errorReportDao;
    private DBI dbi;


    @Before
    public void setup() throws IOException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testyz", "username", "password");
        dbi = new DBI(dataSource);
        handle = dbi.open();
        SchemaLoader.runSQL("/database/error_reports.sql", handle);
        SchemaLoader.runSQL("/database/dare_preproces.sql", handle);
        errorReportDao = dbi.onDemand(ErrorReportDao.class);

        final RecordDao recordDao = dbi.onDemand(RecordDao.class);

        final Record one = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("id-1", "d-1", OaiStatus.AVAILABLE),
                1, 1L);
        final Record two = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("id-2", "d-2", OaiStatus.AVAILABLE),
                1, 2L);
        final Record three = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("id-3", "d-3", OaiStatus.AVAILABLE),
                2, 3L);
        final Record four = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("id-4", "d-4", OaiStatus.AVAILABLE),
                2, 4L);

        recordDao.insertBatch(Stream.of(one, two, three, four).collect(toList()));

        final Iterator<Record> it = recordDao.fetchAllByProcessStatus(ProcessStatus.PENDING.getCode());

        final Record oneUp = it.next();
        final Record twoUp = it.next();
        final Record threeUp = it.next();
        final Record fourUp = it.next();

        oneUp.setState(ProcessStatus.FAILED);
        twoUp.setState(ProcessStatus.FAILED);
        threeUp.setState(ProcessStatus.FAILED);
        fourUp.setState(ProcessStatus.FAILED);
        recordDao.updateState(oneUp);
        recordDao.updateState(twoUp);
        recordDao.updateState(threeUp);
        recordDao.updateState(fourUp);
        errorReportDao.insert(1L, new ErrorReport(new IOException("one"), new URL("http://one.com"), ErrorStatus.INTERNAL_SERVER_ERROR));
        errorReportDao.insert(2L, new ErrorReport(new IOException("two"), new URL("http://two.com"), ErrorStatus.NOT_ACCEPTABLE));
        errorReportDao.insert(3L, new ErrorReport(new IOException("three"), new URL("http://three.com"), ErrorStatus.NOT_FOUND));
        errorReportDao.insert(4L, new ErrorReport(new IOException("four"), new URL("http://four.com"), ErrorStatus.NOT_FOUND));
    }

    @After
    public void tearDown() {
        handle.close();
        dataSource.dispose();
    }

    @Test
    public void getStatusUpdateShouldReturnAnAggregatedRecordStatusUpdate() {
        final ErrorReporter instance = new ErrorReporter(dbi);

        final ErrorStatusUpdate result = instance.getStatusUpdate();

        @SuppressWarnings("unchecked")
        final Map<String,Map<String,Long>> resultData = (Map<String,Map<String,Long>>) result.getData();

        assertThat(resultData.keySet(), containsInAnyOrder("1", "2"));
        assertThat(resultData.get("1").keySet(), containsInAnyOrder("500", "406"));
        assertThat(resultData.get("2").keySet(), containsInAnyOrder("404"));
        assertThat(resultData.get("1").get("500"), is(1L));
        assertThat(resultData.get("1").get("406"), is(1L));
        assertThat(resultData.get("2").get("404"), is(2L));

    }
}