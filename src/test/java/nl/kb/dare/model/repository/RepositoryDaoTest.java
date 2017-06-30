package nl.kb.dare.model.repository;

import nl.kb.dare.SchemaLoader;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;

public class RepositoryDaoTest {
    private static final String URL = "http://oai.gharvester.dans.knaw.nl/";
    private static final String NL_DIDL_COMBINED = "nl_didl_combined";

    private JdbcConnectionPool dataSource;
    private Handle handle;
    private RepositoryDao instance;


    @Before
    public void setup() throws IOException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password");
        final DBI dbi = new DBI(dataSource);
        handle = dbi.open();
        SchemaLoader.runSQL("/database/repositories.sql", handle);
        instance = dbi.onDemand(RepositoryDao.class);
        insertTwo();
    }

    @After
    public void tearDown() {
        handle.close();
        dataSource.dispose();
    }

    @Test
    public void insertShouldInsertARepository() {
        final Repository input = new Repository(URL, "Utrecht", NL_DIDL_COMBINED, "uu:dare", null, false, HarvestSchedule.DAILY);
        instance.insert(input);

        final Repository result = instance.findById(3);

        assertThat(result, allOf(
                hasProperty("name", is(input.getName())),
                hasProperty("dateStamp", is(input.getDateStamp())),
                hasProperty("enabled", is(input.getEnabled())),
                hasProperty("metadataPrefix", is(input.getMetadataPrefix())),
                hasProperty("set", is(input.getSet())),
                hasProperty("schedule", is(input.getSchedule())),
                hasProperty("lastHarvest", is(nullValue()))
        ));
    }

    @Test
    public void removeShouldDeleteARepository() {

        instance.remove(1);

        final List<Repository> result = instance.list();
        assertThat(result.size(), is(1));
        assertThat(result.get(0), hasProperty("id", is(2)));
    }

    @Test
    public void updateShouldUpdateTheRepository() {
        final Repository input = new Repository(URL, "Utrecht", NL_DIDL_COMBINED, "uu:dare", null, false, HarvestSchedule.DAILY);
        instance.update(2, input);

        final Repository result = instance.findById(2);

        assertThat(result, allOf(
                hasProperty("name", is(input.getName())),
                hasProperty("dateStamp", is(input.getDateStamp())),
                hasProperty("enabled", is(input.getEnabled())),
                hasProperty("metadataPrefix", is(input.getMetadataPrefix())),
                hasProperty("set", is(input.getSet())),
                hasProperty("schedule", is(input.getSchedule())),
                hasProperty("lastHarvest", is(nullValue()))
        ));
    }

    @Test
    public void enableShouldSetTheEnabledFieldToTrue() {
        instance.enable(1);

        final Repository result = instance.findById(1);

        assertThat(result, hasProperty("enabled", is(true)));
    }

    @Test
    public void disableShouldSetTheEnabledFieldToFalse() {
        instance.disable(2);

        final Repository result = instance.findById(2);

        assertThat(result, hasProperty("enabled", is(false)));
    }

    @Test
    public void disableAllShouldSetTheEnabledFieldToFalse() {
        instance.insert(new Repository(URL, "three", "three", "three", "three", true, HarvestSchedule.DAILY));

        instance.disableAll();

        assertThat(instance.list().stream().filter(Repository::getEnabled).count(), is(0L));
        assertThat(instance.list().stream().filter(repo -> !repo.getEnabled()).count(), is(3L));
    }

    @Test
    public void setDateStampShouldSetTheDatestampField() {
        instance.setDateStamp(1, "new-datestamp");

        final Repository result = instance.findById(1);

        assertThat(result.getDateStamp(), is("new-datestamp"));
    }

    @Test
    public void setLastHarvestShouldSetTheCurrentTime() {
        instance.setLastHarvest(1);

        final Repository result = instance.findById(1);

        assertThat(result.getLastHarvest(), is(instanceOf(LocalDate.class)));
    }

    private void insertTwo() {
        instance.insert(new Repository(URL, "One", "one", "one", "one", false, HarvestSchedule.DAILY));
        instance.insert(new Repository(URL, "Two", "two", "two", "two", true, HarvestSchedule.DAILY));
    }


}