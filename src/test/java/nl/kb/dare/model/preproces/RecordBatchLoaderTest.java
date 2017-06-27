package nl.kb.dare.model.preproces;

import nl.kb.dare.model.SocketNotifier;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.nbn.NumbersController;
import nl.kb.oaipmh.OaiRecordHeader;
import nl.kb.oaipmh.OaiStatus;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class RecordBatchLoaderTest {

    @Test
    public void addToBatchShouldUpdateExistingPendingRecordsWhichAreDeletedLater() {
        final String theId = "the-id";
        final RecordDao recordDao = mock(RecordDao.class);
        final Record existing = mock(Record.class);
        final OaiRecordHeader recordHeader = mock(OaiRecordHeader.class);
        final RecordBatchLoader instance = new RecordBatchLoader(recordDao, mock(NumbersController.class),
                mock(RecordReporter.class), mock(SocketNotifier.class));


        when(existing.getState()).thenReturn(ProcessStatus.PENDING.getCode());
        when(recordHeader.getOaiStatus()).thenReturn(OaiStatus.DELETED);
        when(recordHeader.getIdentifier()).thenReturn(theId);
        when(recordDao.findByOaiId(theId)).thenReturn(existing);

        instance.addToBatch(1, recordHeader);
        final InOrder inOrder = inOrder(existing, recordDao);

        inOrder.verify(existing).setState(ProcessStatus.DELETED);
        inOrder.verify(recordDao).updateState(existing);
    }

    @Test
    public void addToBatchShouldUpdateExistingFailedRecordsWhichAreDeletedLater() {
        final String theId = "the-id";
        final RecordDao recordDao = mock(RecordDao.class);
        final Record existing = mock(Record.class);
        final OaiRecordHeader recordHeader = mock(OaiRecordHeader.class);
        final RecordBatchLoader instance = new RecordBatchLoader(recordDao, mock(NumbersController.class),
                mock(RecordReporter.class), mock(SocketNotifier.class));


        when(existing.getState()).thenReturn(ProcessStatus.FAILED.getCode());
        when(recordHeader.getOaiStatus()).thenReturn(OaiStatus.DELETED);
        when(recordHeader.getIdentifier()).thenReturn(theId);
        when(recordDao.findByOaiId(theId)).thenReturn(existing);

        instance.addToBatch(1, recordHeader);

        final InOrder inOrder = inOrder(existing, recordDao);
        inOrder.verify(existing).setState(ProcessStatus.DELETED);
        inOrder.verify(recordDao).updateState(existing);
    }


    @Test
    public void addToBatchShouldNotUpdateExistingProcessedRecordsWhichAreDeletedLater() {
        final String theId = "the-id";
        final RecordDao recordDao = mock(RecordDao.class);
        final Record existing = mock(Record.class);
        final OaiRecordHeader recordHeader = mock(OaiRecordHeader.class);
        final RecordBatchLoader instance = new RecordBatchLoader(recordDao, mock(NumbersController.class),
                mock(RecordReporter.class), mock(SocketNotifier.class));

        when(existing.getState()).thenReturn(ProcessStatus.PROCESSED.getCode());
        when(recordHeader.getOaiStatus()).thenReturn(OaiStatus.DELETED);
        when(recordHeader.getIdentifier()).thenReturn(theId);
        when(recordDao.findByOaiId(theId)).thenReturn(existing);

        instance.addToBatch(1, recordHeader);

        final InOrder inOrder = inOrder(existing, recordDao);
        inOrder.verify(recordDao).findByOaiId(theId);
        inOrder.verify(existing, times(2)).getState();
        verifyNoMoreInteractions(existing);
        verifyNoMoreInteractions(recordDao);
    }
}