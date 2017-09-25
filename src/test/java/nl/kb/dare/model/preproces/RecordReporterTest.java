package nl.kb.dare.model.preproces;

import nl.kb.dare.databasetasks.SchemaLoader;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.websocket.socketupdate.RecordStatusUpdate;
import nl.kb.oaipmh.OaiStatus;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class RecordReporterTest {
    private JdbcConnectionPool dataSource;
    private Handle handle;
    private DBI dbi;


    @Before
    public void setup() throws IOException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testx", "username", "password");
        dbi = new DBI(dataSource);
        handle = dbi.open();
        SchemaLoader.runSQL("/database/dare_preproces.sql", handle);
        final RecordDao recordDao = dbi.onDemand(RecordDao.class);

        final Record one = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("id-1", "d-1", OaiStatus.AVAILABLE),
                1, "1");
        final Record two = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("id-2", "d-2", OaiStatus.AVAILABLE),
                1, "2");
        final Record three = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("id-3", "d-3", OaiStatus.AVAILABLE),
                2, "3");
        final Record four = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("id-4", "d-4", OaiStatus.AVAILABLE),
                2, "4");

        recordDao.insertBatch(Stream.of(one, two, three, four).collect(toList()));

        final Iterator<Record> it = recordDao.fetchAllByProcessStatus(ProcessStatus.PENDING.getCode());

        final Record oneUp = it.next();
        final Record twoUp = it.next();
        final Record threeUp = it.next();

        oneUp.setState(ProcessStatus.PROCESSED);
        twoUp.setState(ProcessStatus.DELETED);
        threeUp.setState(ProcessStatus.FAILED);
        recordDao.updateState(oneUp);
        recordDao.updateState(twoUp);
        recordDao.updateState(threeUp);
    }


    @After
    public void tearDown() {
        handle.close();
        dataSource.dispose();
    }

    @Test
    public void getStatusUpdateShouldReturnAnAggregatedRecordStatusUpdate() {
        final RecordReporter instance = new RecordReporter(dbi);

        final RecordStatusUpdate result = instance.getStatusUpdate();

        @SuppressWarnings("unchecked")
        final Map<String,Map<String,Long>> resultData = (Map<String,Map<String,Long>>) result.getData();

        assertThat(resultData.keySet(), containsInAnyOrder("1", "2"));
        assertThat(resultData.get("1").keySet(), containsInAnyOrder("processed", "deleted"));
        assertThat(resultData.get("2").keySet(), containsInAnyOrder("pending", "failure"));
        assertThat(resultData.get("1").get("processed"), is(1L));
        assertThat(resultData.get("1").get("deleted"), is(1L));
        assertThat(resultData.get("2").get("pending"), is(1L));
        assertThat(resultData.get("2").get("failure"), is(1L));

    }
}