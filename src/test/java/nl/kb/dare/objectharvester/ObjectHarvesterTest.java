package nl.kb.dare.objectharvester;

import com.google.common.collect.Lists;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.ErrorReporter;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.manifest.ObjectResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ObjectHarvesterTest {

    private static final Consumer<ErrorReport> onError = errorReport -> {};
    private RepositoryDao repositoryDao = mock(RepositoryDao.class);
    private RecordDao recordDao = mock(RecordDao.class);
    private ErrorReportDao errorReportDao = mock(ErrorReportDao.class);
    private SocketNotifier socketNotifier = mock(SocketNotifier.class);
    private ErrorReporter errorReporter = mock(ErrorReporter.class);
    private RecordReporter recordReporter = mock(RecordReporter.class);
    private Integer maxSequentialDownloadFailures = 10;
    private Repository repositoryConfig = mock(Repository.class);

    @Before
    public void setUp() {
        when(repositoryConfig.getSet()).thenReturn("set:name");
    }

    @Test
    public void fetchShouldReturnFailedWhenNoFileStorageHandleCouldBeCreated()  {
        final ObjectHarvesterOperations getRecordOperations = mock(ObjectHarvesterOperations.class);
        final Record oaiRecord = mock(Record.class);
        final ObjectHarvester instance = new ObjectHarvester.Builder()
                .setRepositoryDao(repositoryDao)
                .setRecordDao(recordDao)
                .setErrorReportDao(errorReportDao)
                .setObjectHarvesterOperations(getRecordOperations)
                .setRecordReporter(recordReporter)
                .setErrorReporter(errorReporter)
                .setSocketNotifier(socketNotifier)
                .setMaxSequentialDownloadFailures(maxSequentialDownloadFailures)
                .setObjectHarvestErrorFlowHandler(mock(ObjectHarvestErrorFlowHandler.class))
                .create();
        when(getRecordOperations.getFileStorageHandle(any(), any(), any())).thenReturn(Optional.empty());

        final ProcessStatus result = instance.harvestPublication(oaiRecord, repositoryConfig, onError);
        verify(getRecordOperations).getFileStorageHandle("set", oaiRecord, onError);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenDownloadMetadataFails()  {
        final ObjectHarvesterOperations getRecordOperations = mock(ObjectHarvesterOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ObjectHarvester instance = new ObjectHarvester.Builder()
                .setRepositoryDao(repositoryDao)
                .setRecordDao(recordDao)
                .setErrorReportDao(errorReportDao)
                .setObjectHarvesterOperations(getRecordOperations)
                .setRecordReporter(recordReporter)
                .setErrorReporter(errorReporter)
                .setSocketNotifier(socketNotifier)
                .setMaxSequentialDownloadFailures(maxSequentialDownloadFailures)
                .setObjectHarvestErrorFlowHandler(mock(ObjectHarvestErrorFlowHandler.class))
                .create();

        when(getRecordOperations.getFileStorageHandle(any(), any(), any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any(), any(), any())).thenReturn(Optional.empty());

        final ProcessStatus result = instance.harvestPublication(oaiRecord, repositoryConfig, onError);

        final InOrder inOrder = inOrder(getRecordOperations, getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle("set", oaiRecord, onError);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord, repositoryConfig, onError);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenGenerateManifestFails() {
        final ObjectHarvesterOperations getRecordOperations = mock(ObjectHarvesterOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ObjectHarvester instance = new ObjectHarvester.Builder()
                .setRepositoryDao(repositoryDao)
                .setRecordDao(recordDao)
                .setErrorReportDao(errorReportDao)
                .setObjectHarvesterOperations(getRecordOperations)
                .setRecordReporter(recordReporter)
                .setErrorReporter(errorReporter)
                .setSocketNotifier(socketNotifier)
                .setMaxSequentialDownloadFailures(maxSequentialDownloadFailures)
                .setObjectHarvestErrorFlowHandler(mock(ObjectHarvestErrorFlowHandler.class))
                .create();
        when(getRecordOperations.getFileStorageHandle(any(), any(), any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any(), any(), any())).thenReturn(Optional.of(mock(ObjectResource.class)));
        when(getRecordOperations.generateManifest(fileStorageHandle, onError)).thenReturn(false);

        final ProcessStatus result = instance.harvestPublication(oaiRecord, repositoryConfig, onError);

        final InOrder inOrder = inOrder(getRecordOperations, getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle("set", oaiRecord, onError);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord, repositoryConfig, onError);
        inOrder.verify(getRecordOperations).generateManifest(fileStorageHandle, onError);

        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenDownloadResourcesFails()  {
        final ObjectHarvesterOperations getRecordOperations = mock(ObjectHarvesterOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final List<ObjectResource> objectResources = Lists.newArrayList(mock(ObjectResource.class));
        final ObjectHarvester instance = new ObjectHarvester.Builder()
                .setRepositoryDao(repositoryDao)
                .setRecordDao(recordDao)
                .setErrorReportDao(errorReportDao)
                .setObjectHarvesterOperations(getRecordOperations)
                .setRecordReporter(recordReporter)
                .setErrorReporter(errorReporter)
                .setSocketNotifier(socketNotifier)
                .setMaxSequentialDownloadFailures(maxSequentialDownloadFailures)
                .setObjectHarvestErrorFlowHandler(mock(ObjectHarvestErrorFlowHandler.class))
                .create();

        when(getRecordOperations.getFileStorageHandle(any(), any(), any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.generateManifest(fileStorageHandle, onError)).thenReturn(true);
        when(getRecordOperations.collectResources(any(), any())).thenReturn(objectResources);
        when(getRecordOperations.downloadMetadata(any(), any(), any(), any())).thenReturn(Optional.of(mock(ObjectResource.class)));
        when(getRecordOperations.downloadResources(any(), any(), any())).thenReturn(false);

        final ProcessStatus result = instance.harvestPublication(oaiRecord, repositoryConfig, onError);

        final InOrder inOrder = inOrder(getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle("set", oaiRecord, onError);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord, repositoryConfig, onError);
        inOrder.verify(getRecordOperations).generateManifest(fileStorageHandle, onError);
        inOrder.verify(getRecordOperations).collectResources(fileStorageHandle, onError);
        inOrder.verify(getRecordOperations).downloadResources(fileStorageHandle, objectResources, onError);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnFailedWhenWriteFilenamesAndChecksumsToMetadataFails()  {
        final ObjectHarvesterOperations getRecordOperations = mock(ObjectHarvesterOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final List<ObjectResource> objectResources = Lists.newArrayList(mock(ObjectResource.class));
        final ObjectHarvester instance = new ObjectHarvester.Builder()
                .setRepositoryDao(repositoryDao)
                .setRecordDao(recordDao)
                .setErrorReportDao(errorReportDao)
                .setObjectHarvesterOperations(getRecordOperations)
                .setRecordReporter(recordReporter)
                .setErrorReporter(errorReporter)
                .setSocketNotifier(socketNotifier)
                .setMaxSequentialDownloadFailures(maxSequentialDownloadFailures)
                .setObjectHarvestErrorFlowHandler(mock(ObjectHarvestErrorFlowHandler.class))
                .create();

        final ObjectResource metadataResource = mock(ObjectResource.class);

        when(getRecordOperations.getFileStorageHandle(any(), any(), any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.generateManifest(fileStorageHandle, onError)).thenReturn(true);
        when(getRecordOperations.collectResources(any(), any())).thenReturn(objectResources);
        when(getRecordOperations.downloadMetadata(any(), any(), any(), any())).thenReturn(Optional.of(metadataResource));
        when(getRecordOperations.downloadResources(any(), any(), any())).thenReturn(true);
        when(getRecordOperations.writeFilenamesAndChecksumsToMetadata(any(), any(), any(), any())).thenReturn(false);

        final ProcessStatus result = instance.harvestPublication(oaiRecord, repositoryConfig, onError);

        final InOrder inOrder = inOrder(getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle("set", oaiRecord, onError);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord, repositoryConfig, onError);
        inOrder.verify(getRecordOperations).generateManifest(fileStorageHandle, onError);
        inOrder.verify(getRecordOperations).collectResources(fileStorageHandle, onError);
        inOrder.verify(getRecordOperations).downloadResources(fileStorageHandle, objectResources, onError);
        inOrder.verify(getRecordOperations).writeFilenamesAndChecksumsToMetadata(fileStorageHandle, objectResources,
                metadataResource, onError);

        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.FAILED));
    }

    @Test
    public void fetchShouldReturnProcessedWhenAllOperationsSucceed()  {
        final ObjectHarvesterOperations getRecordOperations = mock(ObjectHarvesterOperations.class);
        final Record oaiRecord = mock(Record.class);
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ObjectResource metadataResource = mock(ObjectResource.class);
        final List<ObjectResource> objectResources = Lists.newArrayList(mock(ObjectResource.class));
        final ObjectHarvester instance = new ObjectHarvester.Builder()
                .setRepositoryDao(repositoryDao)
                .setRecordDao(recordDao)
                .setErrorReportDao(errorReportDao)
                .setObjectHarvesterOperations(getRecordOperations)
                .setRecordReporter(recordReporter)
                .setErrorReporter(errorReporter)
                .setSocketNotifier(socketNotifier)
                .setMaxSequentialDownloadFailures(maxSequentialDownloadFailures)
                .setObjectHarvestErrorFlowHandler(mock(ObjectHarvestErrorFlowHandler.class))
                .create();
        when(getRecordOperations.getFileStorageHandle(any(), any(), any())).thenReturn(Optional.of(fileStorageHandle));
        when(getRecordOperations.downloadMetadata(any(), any(), any(), any())).thenReturn(Optional.of(metadataResource));
        when(getRecordOperations.generateManifest(fileStorageHandle, onError)).thenReturn(true);
        when(getRecordOperations.collectResources(fileStorageHandle, onError)).thenReturn(objectResources);
        when(getRecordOperations.downloadResources(any(), any(), any())).thenReturn(true);
        when(getRecordOperations.writeFilenamesAndChecksumsToMetadata(any(), any(), any(), any())).thenReturn(true);

        final ProcessStatus result = instance.harvestPublication(oaiRecord, repositoryConfig, onError);

        final InOrder inOrder = inOrder(getRecordOperations);
        inOrder.verify(getRecordOperations).getFileStorageHandle("set", oaiRecord, onError);
        inOrder.verify(getRecordOperations).downloadMetadata(fileStorageHandle, oaiRecord, repositoryConfig, onError);
        inOrder.verify(getRecordOperations).generateManifest(fileStorageHandle, onError);
        inOrder.verify(getRecordOperations).collectResources(fileStorageHandle, onError);
        inOrder.verify(getRecordOperations).downloadResources(fileStorageHandle, objectResources, onError);
        inOrder.verify(getRecordOperations).writeFilenamesAndChecksumsToMetadata(fileStorageHandle, objectResources,
                metadataResource, onError);
        verifyNoMoreInteractions(getRecordOperations);

        assertThat(result, is(ProcessStatus.PROCESSED));
    }
}