package nl.kb.dare.jobs.getrecord;

import com.google.common.collect.Lists;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.manifest.ObjectResource;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class GetRecordTest {

    @Test
    public void fetchShouldReturnFailedWhenNoFileStorageHandleCouldBeCreated()  {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final Record oaiRecord = mock(Record.class);
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord);
        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.empty());

        final ProcessStatus result = instance.fetch();
        verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenDownloadMetadataFails()  {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord);
        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any())).thenReturn(Optional.empty());

        final ProcessStatus result = instance.fetch();

        final InOrder inOrder = inOrder(getRecordOperations, getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenGenerateManifestFails() {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord);
        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any())).thenReturn(Optional.of(mock(ObjectResource.class)));
        when(getRecordOperations.generateManifest(fileStorageHandle)).thenReturn(false);

        final ProcessStatus result = instance.fetch();

        final InOrder inOrder = inOrder(getRecordOperations, getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord);
        inOrder.verify(getRecordOperations).generateManifest(fileStorageHandle);

        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenDownloadResourcesFails()  {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final List<ObjectResource> objectResources = Lists.newArrayList(mock(ObjectResource.class));
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord);

        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.generateManifest(fileStorageHandle)).thenReturn(true);
        when(getRecordOperations.collectResources(any())).thenReturn(objectResources);
        when(getRecordOperations.downloadMetadata(any(), any())).thenReturn(Optional.of(mock(ObjectResource.class)));
        when(getRecordOperations.downloadResources(any(), any())).thenReturn(false);

        final ProcessStatus result = instance.fetch();

        final InOrder inOrder = inOrder(getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord);
        inOrder.verify(getRecordOperations).generateManifest(fileStorageHandle);
        inOrder.verify(getRecordOperations).collectResources(fileStorageHandle);
        inOrder.verify(getRecordOperations).downloadResources(fileStorageHandle, objectResources);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenWriteFilenamesAndChecksumsToMetadataFails()  {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final List<ObjectResource> objectResources = Lists.newArrayList(mock(ObjectResource.class));
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord);
        final ObjectResource metadataResource = mock(ObjectResource.class);

        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.generateManifest(fileStorageHandle)).thenReturn(true);
        when(getRecordOperations.collectResources(any())).thenReturn(objectResources);
        when(getRecordOperations.downloadMetadata(any(), any())).thenReturn(Optional.of(metadataResource));
        when(getRecordOperations.downloadResources(any(), any())).thenReturn(true);
        when(getRecordOperations.writeFilenamesAndChecksumsToMetadata(any(), any(), any())).thenReturn(false);

        final ProcessStatus result = instance.fetch();

        final InOrder inOrder = inOrder(getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord);
        inOrder.verify(getRecordOperations).generateManifest(fileStorageHandle);
        inOrder.verify(getRecordOperations).collectResources(fileStorageHandle);
        inOrder.verify(getRecordOperations).downloadResources(fileStorageHandle, objectResources);
        inOrder.verify(getRecordOperations).writeFilenamesAndChecksumsToMetadata(fileStorageHandle, objectResources,
                metadataResource);

        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnProcessedWhenAllOperationsSucceed()  {
        final GetRecordOperations getRecordOperations = mock(GetRecordOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ObjectResource metadataResource = mock(ObjectResource.class);
        final List<ObjectResource> objectResources = Lists.newArrayList(mock(ObjectResource.class));
        final GetRecord instance = new GetRecord(getRecordOperations, oaiRecord);
        when(getRecordOperations.getFileStorageHandle(any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any())).thenReturn(Optional.of(metadataResource));
        when(getRecordOperations.generateManifest(fileStorageHandle)).thenReturn(true);
        when(getRecordOperations.collectResources(fileStorageHandle)).thenReturn(objectResources);
        when(getRecordOperations.downloadResources(any(), any())).thenReturn(true);
        when(getRecordOperations.writeFilenamesAndChecksumsToMetadata(any(), any(), any())).thenReturn(true);

        final ProcessStatus result = instance.fetch();

        final InOrder inOrder = inOrder(getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle(oaiRecord);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord);
        inOrder.verify(getRecordOperations).generateManifest(fileStorageHandle);
        inOrder.verify(getRecordOperations).collectResources(fileStorageHandle);
        inOrder.verify(getRecordOperations).downloadResources(fileStorageHandle, objectResources);
        inOrder.verify(getRecordOperations).writeFilenamesAndChecksumsToMetadata(fileStorageHandle, objectResources,
                metadataResource);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.PROCESSED));
    }
}