package nl.kb.dare.model.preproces;

import nl.kb.dare.SchemaLoader;
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
        SchemaLoader.runSQL("/database-schema/h2/dare_preproces.sql", handle);
        instance = dbi.onDemand(RecordDao.class);

    }

    @After
    public void tearDown() {
        handle.close();
        dataSource.dispose();
    }

    @Test
    public void insertBatchShouldInsertTheRecords() {
        final Record one = Record.fromHeader(
                makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE), 1);
        final Record two = Record.fromHeader(
                makeRecordHeader("oai-id-2", "oai-d-2", OaiStatus.AVAILABLE), 2);
        one.setKbObjId(1L);
        two.setKbObjId(2L);

        instance.insertBatch(Stream.of(one, two).collect(Collectors.toList()));

        final List<Record> result = new ArrayList<>();
        instance.fetchAllByProcessStatus(ProcessStatus.PENDING.getCode()).forEachRemaining(result::add);

        assertThat(result, containsInAnyOrder(
                allOf(
                        hasProperty("kbObjId", is("1")),
                        hasProperty("repositoryId", is(1)),
                        hasProperty("state", is(ProcessStatus.PENDING.getCode())),
                        hasProperty("oaiIdentifier", is("oai-id-1")),
                        hasProperty("oaiDateStamp", is("oai-d-1")),
                        hasProperty("tsCreate", is(instanceOf(String.class))),
                        hasProperty("tsProcessed", is(nullValue()))
                ),
                allOf(
                        hasProperty("kbObjId", is("2")),
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
        final Record existingRecord = Record.fromHeader(
                makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE), 1);
        final OaiRecordHeader newRecordHeader =
                makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE);
        existingRecord.setKbObjId(1L);
        instance.insertBatch(Stream.of(existingRecord).collect(Collectors.toList()));

        final Boolean result = instance.existsByDatestampAndIdentifier(newRecordHeader);

        assertThat(result, is(true));
    }

    @Test
    public void existsByDatestampAndIdentifierShouldReturnFalseOtherwise() {
        final Record existingRecord = Record.fromHeader(
                makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE), 1);
        final OaiRecordHeader newRecordHeader =
                makeRecordHeader("oai-id-1", "oai-d-2", OaiStatus.AVAILABLE);
        final OaiRecordHeader newRecordHeader2 =
                makeRecordHeader("oai-id-2", "oai-d-1", OaiStatus.AVAILABLE);
        existingRecord.setKbObjId(1L);
        instance.insertBatch(Stream.of(existingRecord).collect(Collectors.toList()));

        final Boolean result1 = instance.existsByDatestampAndIdentifier(newRecordHeader);
        final Boolean result2 = instance.existsByDatestampAndIdentifier(newRecordHeader2);

        assertThat(result1, is(false));
        assertThat(result2, is(false));
    }

    @Test
    public void fetchNextWithProcessStatusByRepositoryIdShouldFilterTheResultByProcessStatusAndRepositoryId()
            throws IOException {
        final Record one = Record.fromHeader(
                makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE), 1);
        final Record two = Record.fromHeader(
                makeRecordHeader("oai-id-2", "oai-d-2", OaiStatus.AVAILABLE), 2);
        final Record three = Record.fromHeader(
                makeRecordHeader("oai-id-3", "oai-d-3", OaiStatus.AVAILABLE), 1);
        one.setKbObjId(1L);
        two.setKbObjId(2L);
        three.setKbObjId(3L);
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
        final Record one = Record.fromHeader(
                makeRecordHeader("oai-id-1", "oai-d-1", OaiStatus.AVAILABLE), 1);
        one.setKbObjId(1L);
        instance.insertBatch(Stream.of(one).collect(Collectors.toList()));
        final Record toUpdate = instance.findByOaiId("oai-id-1");
        toUpdate.setState(ProcessStatus.PROCESSED);

        instance.updateState(toUpdate);

        final Record result = instance.findByKbObjId("1");
        assertThat(result, allOf(
                hasProperty("state", is(ProcessStatus.PROCESSED.getCode())),
                hasProperty("tsProcessed", not(isEmptyOrNullString()))
        ));
    }

    private OaiRecordHeader makeRecordHeader(String oaiId, String oaiDateStamp, OaiStatus oaiStatus) {
        final OaiRecordHeader result = new OaiRecordHeader();
        result.setDateStamp(oaiDateStamp);
        result.setIdentifier(oaiId);
        result.setOaiStatus(oaiStatus);
        return result;
    }

}