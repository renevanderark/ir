package nl.kb.dare.integrationtest;

import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.kb.dare.App;
import nl.kb.dare.integrationtest.crud.CrudOperations;
import nl.kb.dare.integrationtest.numbers.NumbersTestServer;
import nl.kb.dare.integrationtest.oai.OaiTestServer;
import nl.kb.dare.integrationtest.util.IntegrationTestUtil;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordMapper;
import nl.kb.dare.model.repository.HarvestSchedule;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryMapper;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

public class HarvesterHappyFlowTest {

    @ClassRule
    public static final TestRule instanceRule;

    @ClassRule
    public static final TestRule oaiRule;

    @ClassRule
    public static final TestRule numbersRule;

    static  {
        try {
            instanceRule  = new DropwizardAppRule<>(App.class, Paths.get(HarvesterHappyFlowTest.class
                    .getResource("/integrationtest/dare-app-config.yaml").toURI()).toString());
            oaiRule = new DropwizardAppRule<>(OaiTestServer.class, Paths.get(HarvesterHappyFlowTest.class.
                    getResource("/integrationtest/oai-test-server.yaml").toURI()).toString());

            numbersRule = new DropwizardAppRule<>(NumbersTestServer.class, Paths.get(HarvesterHappyFlowTest.class.
                    getResource("/integrationtest/numbers-test-server.yaml").toURI()).toString());

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private DBI db;
    private Handle dbh;

    @Before
    public void setUp() throws SQLException, IOException, ClassNotFoundException, URISyntaxException {
        CrudOperations.createH2Schema();
        db = new DBI("jdbc:h2:mem:dareintegration", "daredev", "daredev");
        dbh = db.open();
        dbh.execute("TRUNCATE TABLE DARE_PREPROCES");
        dbh.execute("TRUNCATE TABLE REPOSITORIES");

    }

    @After
    public void tearDown() {
        db.close(dbh);
    }

    @Test
    public void run() throws InterruptedException, IOException {

        final String location = CrudOperations.createRepository(IntegrationTestUtil.getRepositoryPayload("/integrationtest/payloads/repository.json"));
        assertRepositoryCreated(location);

        final int enableRepoStatusCode = CrudOperations.enableRepository(1);
        assertRepositoryEnabled(enableRepoStatusCode, 1);

        final int startHarvestStatusCode = CrudOperations.startHarvest(1);
        assertThat(startHarvestStatusCode, is(200));

        Thread.sleep(1000L);
        assertRecordsInserted();
        assertLastHarvestDateRecorded(1);
    }

    private void assertRecordsInserted() {
        final List<Record> records = dbh.createQuery("select * from dare_preproces")
                .map(new RecordMapper())
                .list();

        assertThat(records, containsInAnyOrder(
                allOf(
                        hasProperty("id", is(1L)),
                        hasProperty("repositoryId", is(1)),
                        hasProperty("oaiIdentifier", is("ru:oai:repository.ubn.ru.nl:2066/162830")),
                        hasProperty("kbObjId", is(instanceOf(String.class))),
                        hasProperty("state", is(ProcessStatus.DELETED.getCode())),
                        hasProperty("tsCreate", is(instanceOf(String.class))),
                        hasProperty("tsProcessed", is(instanceOf(String.class)))
                ),
                allOf(
                        hasProperty("id", is(2L)),
                        hasProperty("repositoryId", is(1)),
                        hasProperty("oaiIdentifier", is("ru:oai:repository.ubn.ru.nl:2066/162526")),
                        hasProperty("kbObjId", is(instanceOf(String.class))),
                        hasProperty("state", is(ProcessStatus.PENDING.getCode())),
                        hasProperty("tsCreate", is(instanceOf(String.class))),
                        hasProperty("tsProcessed", is(nullValue()))
                ),
                allOf(
                        hasProperty("id", is(3L)),
                        hasProperty("repositoryId", is(1)),
                        hasProperty("oaiIdentifier", is("ru:oai:repository.ubn.ru.nl:2066/161830")),
                        hasProperty("kbObjId", is(instanceOf(String.class))),
                        hasProperty("state", is(ProcessStatus.PENDING.getCode())),
                        hasProperty("tsCreate", is(instanceOf(String.class))),
                        hasProperty("tsProcessed", is(nullValue()))
                ),
                allOf(
                        hasProperty("id", is(4L)),
                        hasProperty("repositoryId", is(1)),
                        hasProperty("oaiIdentifier", is("ru:oai:repository.ubn.ru.nl:2066/161841")),
                        hasProperty("kbObjId", is(instanceOf(String.class))),
                        hasProperty("state", is(ProcessStatus.PENDING.getCode())),
                        hasProperty("tsCreate", is(instanceOf(String.class))),
                        hasProperty("tsProcessed", is(nullValue()))
                )
        ));

    }

    private void assertRepositoryEnabled(int statusCode, Integer repositoryId) {
        assertThat(statusCode, is(200));

        final Repository result = dbh.createQuery("select * from repositories where id = :id")
                .bind("id", repositoryId)
                .map(new RepositoryMapper())
                .first();

        assertThat(result, hasProperty("enabled", is(true)));
    }

    private void assertLastHarvestDateRecorded(Integer repositoryId) {
        final Repository result = dbh.createQuery("select * from repositories where id = :id")
                .bind("id", repositoryId)
                .map(new RepositoryMapper())
                .first();

        assertThat(result.getLastHarvest(), is(instanceOf(LocalDate.class)));
    }

    private void assertRepositoryCreated(String location) {
        final Repository result = dbh.createQuery("select * from repositories").map(new RepositoryMapper()).first();

        assertThat(location, is(String.format("%s/repositories/1", IntegrationTestUtil.APP_URL)));
        assertThat(result, allOf(
                hasProperty("schedule", is(HarvestSchedule.MONTHLY)),
                hasProperty("enabled", is(false)),
                hasProperty("id", is(1)),
                hasProperty("url", is("http://localhost:18081/oai")),
                hasProperty("set", is("test")),
                hasProperty("metadataPrefix", is("nl_didl_combined")),
                hasProperty("name", is("Testing")),
                hasProperty("dateStamp", is(nullValue())),
                hasProperty("lastHarvest", is(nullValue()))
        ));
    }
}
