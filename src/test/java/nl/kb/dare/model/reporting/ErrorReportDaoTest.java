package nl.kb.dare.model.reporting;

import nl.kb.dare.databasetasks.SchemaLoader;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordUtil;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.oaipmh.OaiStatus;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;


public class ErrorReportDaoTest {

    private JdbcConnectionPool dataSource;
    private Handle handle;
    private ErrorReportDao instance;
    private RecordDao recordDao;


    @Before
    public void setup() throws IOException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testy", "username", "password");
        final DBI dbi = new DBI(dataSource);
        handle = dbi.open();
        SchemaLoader.runSQL("/database/error_reports.sql", handle);
        SchemaLoader.runSQL("/database/dare_preproces.sql", handle);
        instance = dbi.onDemand(ErrorReportDao.class);
        recordDao = dbi.onDemand(RecordDao.class);

    }

    @After
    public void tearDown() {
        handle.close();
        dataSource.dispose();
    }

    @Test
    public void insertShouldInsertTheCorrectFields() throws MalformedURLException {
        try {
            throw new IOException("test exception");
        } catch (IOException e) {
            final ErrorReport errorReport = new ErrorReport(e, new URL("http://example.com"), ErrorStatus.IO_EXCEPTION);
            instance.insert(1L, errorReport);

            final StoredErrorReport result = instance.fetchForRecordId(1L);

            assertThat(result, allOf(
                hasProperty("statusCode", is(ErrorStatus.IO_EXCEPTION.getCode())),
                hasProperty("message", is("test exception")),
                hasProperty("url", is("http://example.com")),
                hasProperty("stackTrace", containsString(this.getClass().getCanonicalName()))
            ));
        }
    }

    @Test
    public void deleteForRecordIdShouldDeleteTheErrorReportForTheGivenRecordId() throws MalformedURLException {
        try {
            throw new IOException("test exception");
        } catch (IOException e) {
            final ErrorReport errorReport = new ErrorReport(e, new URL("http://example.com"),
                    ErrorStatus.IO_EXCEPTION);
            instance.insert(1L, errorReport);
            final StoredErrorReport res1 = instance.fetchForRecordId(1L);
            instance.deleteForRecordId(1L);
            final StoredErrorReport res2 = instance.fetchForRecordId(1L);

            assertThat(res1, not(is(nullValue())));
            assertThat(res2, is(nullValue()));


        }
    }

    @Test
    public void bulkDeleteForRepositoryShouldDeleteAllErrorReportsForTheRepository() throws MalformedURLException {
        try {
            throw new IOException("test exception");
        } catch (IOException e) {
            final ErrorReport errorReport = new ErrorReport(e, new URL("http://example.com"), ErrorStatus.IO_EXCEPTION);

            final Record one = RecordUtil.makeRecord(
                    RecordUtil.makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE),
                    1,
                    "1"
            );
            final Record two = RecordUtil.makeRecord(
                    RecordUtil.makeRecordHeader("oai-id-2", "oai-d-2", OaiStatus.AVAILABLE),
                    2,
                    "2"
            );
            final Record three = RecordUtil.makeRecord(
                    RecordUtil.makeRecordHeader("oai-id-3", "oai-d32", OaiStatus.AVAILABLE),
                    1,
                    "3"
            );
            recordDao.insertBatch(Stream.of(one, two, three).collect(Collectors.toList()));
            final Record oneUp = recordDao.findByIpName("1");
            final Record twoUp = recordDao.findByIpName("2");
            final Record threeUp = recordDao.findByIpName("3");
            instance.insert(oneUp.getId(), errorReport);
            instance.insert(twoUp.getId(), errorReport);
            instance.insert(threeUp.getId(), errorReport);

            instance.bulkDeleteForRepository(ProcessStatus.PENDING.getCode(), 1);

            final StoredErrorReport result1 = instance.fetchForRecordId(oneUp.getId());
            final StoredErrorReport result2 = instance.fetchForRecordId(twoUp.getId());
            final StoredErrorReport result3 = instance.fetchForRecordId(threeUp.getId());

            assertThat(result1, is(nullValue()));
            assertThat(result2, is(instanceOf(StoredErrorReport.class)));
            assertThat(result3, is(nullValue()));
        }

    }
}