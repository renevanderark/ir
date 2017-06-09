package nl.kb.dare.integrationtest;

import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.kb.dare.App;
import nl.kb.dare.integrationtest.crud.CrudOperations;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HarvesterHappyFlow {

    @ClassRule
    public static TestRule instanceRule;

    static  {
        try {
            instanceRule  = new DropwizardAppRule<>(App.class, Paths.get(HarvesterHappyFlow.class
                    .getResource("/integrationtest/dare-app-config.yaml").toURI()).toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setUp() throws SQLException, IOException, ClassNotFoundException, URISyntaxException {
        CrudOperations.createH2Schema();
    }

    @Test
    public void canCreateRepositoryConfig() throws InterruptedException, IOException {
        final String location = CrudOperations.createRepository(IntegrationTestUtil.getRepositoryPayload());

        assertThat(location, is(String.format("%s/repositories/1", IntegrationTestUtil.APP_URL)));
    }


    @Test
    public void canCreateRepositoryConfig2() throws InterruptedException, IOException {
        final String location = CrudOperations.createRepository(IntegrationTestUtil.getRepositoryPayload());

        assertThat(location, is(String.format("%s/repositories/1", IntegrationTestUtil.APP_URL)));
    }

}
