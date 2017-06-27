package nl.kb.dare.integrationtest;

import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.kb.dare.App;
import nl.kb.dare.integrationtest.crud.CrudOperations;
import nl.kb.dare.integrationtest.numbers.NumbersTestServer;
import nl.kb.dare.integrationtest.oai.OaiTestServer;
import nl.kb.dare.integrationtest.util.IntegrationTestUtil;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

public class NumbersCrashTestRunner {

    @ClassRule
    public static final TestRule instanceRule;

    @ClassRule
    public static final TestRule oaiRule;

    @ClassRule
    public static final TestRule numbersRule;

    static  {
        try {
            instanceRule  = new DropwizardAppRule<>(App.class, Paths.get(HarvesterHappyFlowTest.class
                    .getResource("/integrationtest/dare-app-config-with-numbers-crash.yaml").toURI()).toString());
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
        db = new DBI("jdbc:h2:mem:dareintegrationnumberscrash", "daredev", "daredev");
        dbh = db.open();
        dbh.execute("TRUNCATE TABLE DARE_PREPROCES");
        dbh.execute("TRUNCATE TABLE REPOSITORIES");

        CrudOperations.createRepository(IntegrationTestUtil.getRepositoryPayload("/integrationtest/payloads/repository-infinite.json"));
        CrudOperations.createRepository(IntegrationTestUtil.getRepositoryPayload("/integrationtest/payloads/repository.json"));

        CrudOperations.enableRepository(1);
        CrudOperations.enableRepository(2);
    }

    @After
    public void tearDown() {

        db.close(dbh);

    }

    @Test
    @Ignore
    public void run() throws InterruptedException, IOException {
        CrudOperations.startHarvest(1);
        CrudOperations.startHarvest(2);
        Thread.sleep(1000L);

        assertHarvestersHaveStopped();
        assertRepositoriesHaveBeenDisabledAndNoDateStampHasBeenRecorded();
    }

    private void assertRepositoriesHaveBeenDisabledAndNoDateStampHasBeenRecorded() {
        final Repository repo1 = dbh.createQuery("select * from repositories where id = :id")
                .bind("id", 1)
                .map(new RepositoryMapper())
                .first();

        final Repository repo2 = dbh.createQuery("select * from repositories where id = :id")
                .bind("id", 2)
                .map(new RepositoryMapper())
                .first();

        assertThat(repo1, allOf(
                hasProperty("enabled", is(false)),
                hasProperty("dateStamp", is(nullValue()))
        ));

        assertThat(repo2, allOf(
                hasProperty("enabled", is(false)),
                hasProperty("dateStamp", is(nullValue()))
        ));
    }

    private void assertHarvestersHaveStopped() throws IOException {
        final Map<String, Map<String, String>> result = CrudOperations.getHarvesterStatus();
        assertThat(result.get("1").get("runState"), is("WAITING"));
        assertThat(result.get("2").get("runState"), is("WAITING"));
    }




}
