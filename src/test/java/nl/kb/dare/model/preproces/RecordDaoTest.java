package nl.kb.dare.model.preproces;

import nl.kb.dare.databasetasks.SchemaLoader;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.IsNot.not;


public class RecordDaoTest {
    private JdbcConnectionPool dataSource;
    private Handle handle;
    private RecordDao instance;


    @Before
    public void setup() throws IOException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test", "username", "password");
        final DBI dbi = new DBI(dataSource);
        handle = dbi.open();
        SchemaLoader.runSQL("/database/dare_preproces.sql", handle);
        instance = dbi.onDemand(RecordDao.class);

    }

    @After
    public void tearDown() {
        handle.close();
        dataSource.dispose();
    }

    @Test
    public void insertBatchShouldInsertTheRecords() {
        final Record one =  RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE),
                1,
                1L
        );
        final Record two = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-2", "oai-d-2", OaiStatus.AVAILABLE),
                2,
                2L);

        instance.insertBatch(Stream.of(one, two).collect(Collectors.toList()));

        final List<Record> result = new ArrayList<>();
        instance.fetchAllByProcessStatus(ProcessStatus.PENDING.getCode()).forEachRemaining(result::add);

        assertThat(result, containsInAnyOrder(
                allOf(
                        hasProperty("ipName", is("1")),
                        hasProperty("repositoryId", is(1)),
                        hasProperty("state", is(ProcessStatus.PENDING.getCode())),
                        hasProperty("oaiIdentifier", is("oai-id-1")),
                        hasProperty("oaiDateStamp", is("oai-d-1")),
                        hasProperty("tsCreate", is(instanceOf(String.class))),
                        hasProperty("tsProcessed", is(nullValue()))
                ),
                allOf(
                        hasProperty("ipName", is("2")),
                        hasProperty("repositoryId", is(2)),
                        hasProperty("state", is(ProcessStatus.PENDING.getCode())),
                        hasProperty("oaiIdentifier", is("oai-id-2")),
                        hasProperty("oaiDateStamp", is("oai-d-2")),
                        hasProperty("tsCreate", is(instanceOf(String.class))),
                        hasProperty("tsProcessed", is(nullValue()))
                )
        ));
    }

    @Test
    public void existsByDatestampAndIdentifierShouldReturnTrueWhenTheRecordHasTheSameOaiIdAndOaiDatestamp() {
        final Record existingRecord =  RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE),
                1,
                1L
        );
        final OaiRecordHeader newRecordHeader =
                RecordUtil.makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE);
        instance.insertBatch(Stream.of(existingRecord).collect(Collectors.toList()));

        final Boolean result = instance.existsByDatestampAndIdentifier(newRecordHeader);

        assertThat(result, is(true));
    }

    @Test
    public void existsByDatestampAndIdentifierShouldReturnFalseOtherwise() {
        final Record existingRecord = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE),
                1,
                1L
        );
        final OaiRecordHeader newRecordHeader =
                RecordUtil.makeRecordHeader("oai-id-1", "oai-d-2", OaiStatus.AVAILABLE);
        final OaiRecordHeader newRecordHeader2 =
                RecordUtil.makeRecordHeader("oai-id-2", "oai-d-1", OaiStatus.AVAILABLE);
        instance.insertBatch(Stream.of(existingRecord).collect(Collectors.toList()));

        final Boolean result1 = instance.existsByDatestampAndIdentifier(newRecordHeader);
        final Boolean result2 = instance.existsByDatestampAndIdentifier(newRecordHeader2);

        assertThat(result1, is(false));
        assertThat(result2, is(false));
    }

    @Test
    public void fetchNextWithProcessStatusByRepositoryIdShouldFilterTheResultByProcessStatusAndRepositoryId()
            throws IOException {
        final Record one = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE),
                1,
                1L
        );
        final Record two = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-2", "oai-d-2", OaiStatus.AVAILABLE),
                2,
                2L
        );
        final Record three = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-3", "oai-d32", OaiStatus.AVAILABLE),
                1,
                3L
        );


        instance.insertBatch(Stream.of(one, two, three).collect(Collectors.toList()));

        final List<Record> result1 = instance.fetchNextWithProcessStatusByRepositoryId(
                ProcessStatus.PENDING.getCode(), 2, 1);
        final Record toUpdate = result1.get(0);
        toUpdate.setState(ProcessStatus.PROCESSED);
        instance.updateState(toUpdate);
        final List<Record> result2 = instance.fetchNextWithProcessStatusByRepositoryId(
                ProcessStatus.PENDING.getCode(), 2, 1);

        assertThat(result1.size(), is(2));
        assertThat(result2.size(), is(1));
    }

    @Test
    public void updateStateShouldUpdateTheStateAndSetTheProcessedTimestamp() {
        final Record one = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE),
                1,
                1L
        );

        instance.insertBatch(Stream.of(one).collect(Collectors.toList()));
        final Record toUpdate = instance.findByOaiId("oai-id-1");
        toUpdate.setState(ProcessStatus.PROCESSED);

        instance.updateState(toUpdate);

        final Record result = instance.findByIpName("1");
        assertThat(result, allOf(
                hasProperty("state", is(ProcessStatus.PROCESSED.getCode())),
                hasProperty("tsProcessed", not(isEmptyOrNullString()))
        ));
    }

    @Test
    public void bulkUpdateStateShouldUpdateTheStateForAllRecordsWithGivenRepositoryId() {
        final Record one = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE),
                1,
                1L
        );
        final Record two = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-2", "oai-d-2", OaiStatus.AVAILABLE),
                2,
                2L
        );
        final Record three = RecordUtil.makeRecord(
                RecordUtil.makeRecordHeader("oai-id-3", "oai-d32", OaiStatus.AVAILABLE),
                1,
                3L
        );

        instance.insertBatch(Stream.of(one, two, three).collect(Collectors.toList()));
        instance.bulkUpdateState(ProcessStatus.PENDING.getCode(), ProcessStatus.FAILED.getCode(), 1);

        final Record result1 = instance.findByIpName("1");
        final Record result2 = instance.findByIpName("2");
        final Record result3 = instance.findByIpName("3");

        assertThat(result1, hasProperty("state", is(ProcessStatus.FAILED.getCode())));
        assertThat(result2, not(hasProperty("state", is(ProcessStatus.FAILED.getCode()))));
        assertThat(result3, hasProperty("state", is(ProcessStatus.FAILED.getCode())));

    }

}