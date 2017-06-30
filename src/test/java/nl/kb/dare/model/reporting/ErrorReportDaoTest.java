package nl.kb.dare.model.reporting;

import nl.kb.dare.SchemaLoader;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;


public class ErrorReportDaoTest {

    private JdbcConnectionPool dataSource;
    private Handle handle;
    private ErrorReportDao instance;


    @Before
    public void setup() throws IOException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:testy", "username", "password");
        final DBI dbi = new DBI(dataSource);
        handle = dbi.open();
        SchemaLoader.runSQL("/database/error_reports.sql", handle);
        instance = dbi.onDemand(ErrorReportDao.class);
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
}