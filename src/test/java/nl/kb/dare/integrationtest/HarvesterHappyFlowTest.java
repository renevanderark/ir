package nl.kb.dare.integrationtest;

import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.kb.dare.App;
import nl.kb.dare.integrationtest.crud.CrudOperations;
import nl.kb.dare.model.repository.HarvestSchedule;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryMapper;
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

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

public class HarvesterHappyFlowTest {

    @ClassRule
    public static TestRule instanceRule;

    static  {
        try {
            instanceRule  = new DropwizardAppRule<>(App.class, Paths.get(HarvesterHappyFlowTest.class
                    .getResource("/integrationtest/dare-app-config.yaml").toURI()).toString());
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
    }

    @After
    public void tearDown() {
        db.close(dbh);
    }

    @Test
    public void run() throws InterruptedException, IOException {

        final String location = CrudOperations.createRepository(IntegrationTestUtil.getRepositoryPayload());
        assertRepositoryCreated(location);

        final int enableRepoStatusCode = CrudOperations.enableRepository(1);
        assertRepositoryEnabled(enableRepoStatusCode, 1);

        final int startHarvestStatusCode = CrudOperations.startHarvest(1);
        assertThat(startHarvestStatusCode, is(200));

    }

    private void assertRepositoryEnabled(int statusCode, Integer repositoryId) {
        assertThat(statusCode, is(200));

        final Repository result = dbh.createQuery("select * from repositories where id = :id")
                .bind("id", repositoryId)
                .map(new RepositoryMapper())
                .first();

        assertThat(result, hasProperty("enabled", is(true)));
    }

    private void assertRepositoryCreated(String location) {
        final Repository result = dbh.createQuery("select * from repositories").map(new RepositoryMapper()).first();

        assertThat(location, is(String.format("%s/repositories/1", IntegrationTestUtil.APP_URL)));
        assertThat(result, allOf(
                hasProperty("schedule", is(HarvestSchedule.MONTHLY)),
                hasProperty("enabled", is(false)),
                hasProperty("id", is(1)),
                hasProperty("url", is("http://localhost:4569")),
                hasProperty("set", is("test")),
                hasProperty("metadataPrefix", is("nl_didl_norm")),
                hasProperty("name", is("Testing")),
                hasProperty("dateStamp", is(nullValue())),
                hasProperty("lastHarvest", is(nullValue()))
        ));
    }
}
