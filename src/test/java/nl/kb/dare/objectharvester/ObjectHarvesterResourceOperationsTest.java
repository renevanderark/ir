package nl.kb.dare.objectharvester;

import com.google.common.collect.Lists;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseHandler;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.manifest.ObjectResource;
import nl.kb.stream.ByteCountOutputStream;
import nl.kb.stream.ChecksumOutputStream;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class ObjectHarvesterResourceOperationsTest {

    private static final String BASE_URL = "http://example.com/path/";
    private static final String ORIG_ENCODED_FILENAME = "file%201.ext";
    private static final String FULL_URL = BASE_URL + ORIG_ENCODED_FILENAME;
    private static final String EXPECTED_FILENAME = "expected-filename";
    private static final String TRANSFORMED_ENC_FILE_1 = "file+1.ext";
    private static final String TRANSFORMED_ENC_FILE_2 = "file%201.ext";

    @Test
    public void downloadResourceShouldSaveTheFileAndTheChecksum() throws IOException, NoSuchAlgorithmException {
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final ObjectResource objectResource = getObjectResource(FULL_URL);
        final HttpFetcher httpFetcher = mock(HttpFetcher.class);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        when(responseHandlerFactory.getStreamCopyingResponseHandler(any(), any(ChecksumOutputStream.class),
                any(ByteCountOutputStream.class))).thenReturn(responseHandler);
        when(responseHandler.getExceptions()).thenReturn(Lists.newArrayList());

        final ObjectHarvesterResourceOperations instance = new ObjectHarvesterResourceOperations(httpFetcher,
                responseHandlerFactory, fileLocation -> EXPECTED_FILENAME);

        final List<ErrorReport> errorReports = instance.downloadResource(objectResource, fileStorageHandle);


        InOrder inOrder = Mockito.inOrder(objectResource, fileStorageHandle, responseHandlerFactory, httpFetcher);
        // final String fileLocation = objectResource.getXlinkHref();
        inOrder.verify(objectResource).getXlinkHref();
        // final String filename = createFilename(fileLocation); => EXPECTED_FILENAME
        // final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
        inOrder.verify(fileStorageHandle).getOutputStream("resources", EXPECTED_FILENAME);
        // final ByteArrayOutputStream checksumOut = new ByteArrayOutputStream();
        // final List<ErrorReport> firstAttemptErrors = attemptDownload(fileLocation, objectOut, checksumOut, false);
        inOrder.verify(responseHandlerFactory).getStreamCopyingResponseHandler(any(),
                any(ChecksumOutputStream.class), any(ByteCountOutputStream.class));
        inOrder.verify(httpFetcher).execute(
                argThat(allOf(
                        hasProperty("host", is("example.com")),
                        hasProperty("file", is("/path/" + TRANSFORMED_ENC_FILE_1))
                )),
                argThat(is(responseHandler))
        );
        // if (firstAttemptErrors.isEmpty()) {
        // .. writeChecksum(objectResource, checksumOut);
        inOrder.verify(objectResource).setChecksum(argThat(is(instanceOf(String.class))));
        inOrder.verify(objectResource).setChecksumType(argThat(is("SHA-512")));
        inOrder.verify(objectResource).setLocalFilename(EXPECTED_FILENAME);
        // .. return Lists.newArrayList();
        assertThat(errorReports.isEmpty(), is(true));
        // }
    }

    @Test
    public void downloadResourceShouldSaveTheFileAndTheChecksumAfterSecondAttempt() throws IOException, NoSuchAlgorithmException {
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final ObjectResource objectResource = getObjectResource(FULL_URL);
        final HttpFetcher httpFetcher = mock(HttpFetcher.class);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        when(responseHandlerFactory.getStreamCopyingResponseHandler(any(), any(), any()))
                .thenReturn(responseHandler);
        when(responseHandler.getExceptions())
                .thenReturn(Lists.newArrayList(mock(Exception.class)))
                .thenReturn(Lists.newArrayList());

        final ObjectHarvesterResourceOperations instance = new ObjectHarvesterResourceOperations(httpFetcher,
                responseHandlerFactory, fileLocation -> EXPECTED_FILENAME);


        final List<ErrorReport> errorReports = instance
                .downloadResource(objectResource, fileStorageHandle);

        InOrder inOrder = Mockito.inOrder(httpFetcher, responseHandlerFactory, objectResource);

        // final List<ErrorReport> firstAttemptErrors = attemptDownload(fileLocation, objectOut, checksumOut, false);
        inOrder.verify(httpFetcher).execute(any(), any());
        // final List<ErrorReport> secondAttemptErrors = attemptDownload(fileLocation, objectOut, checksumOut, true);
        inOrder.verify(responseHandlerFactory).getStreamCopyingResponseHandler(any(), any(), any());
        inOrder.verify(httpFetcher).execute(
                argThat(allOf(
                        hasProperty("host", is("example.com")),
                        hasProperty("file", is("/path/" + TRANSFORMED_ENC_FILE_2))
                )),
                argThat(is(responseHandler))
        );
        // if (secondAttemptErrors.isEmpty()) {
        // ..  writeChecksum(objectResource, checksumOut);
        inOrder.verify(objectResource).setChecksum(argThat(is(instanceOf(String.class))));
        inOrder.verify(objectResource).setChecksumType(argThat(is("SHA-512")));
        inOrder.verify(objectResource).setLocalFilename(EXPECTED_FILENAME);
        // ..  return Lists.newArrayList();
        assertThat(errorReports.isEmpty(), is(true));
        // }
    }

    @Test
    public void itShouldReturnAllTheErrorReportsOfBothFailedAttempts() throws IOException, NoSuchAlgorithmException {
        final FileStorageHandle fileStorageHandle = mock(FileStorageHandle.class);
        final ResponseHandlerFactory responseHandlerFactory = mock(ResponseHandlerFactory.class);
        final ObjectResource objectResource = getObjectResource(FULL_URL);
        final HttpFetcher httpFetcher = mock(HttpFetcher.class);
        final ObjectHarvesterResourceOperations instance = new ObjectHarvesterResourceOperations(httpFetcher, responseHandlerFactory);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);

        when(responseHandlerFactory.getStreamCopyingResponseHandler(any(), any(), any()))
                .thenReturn(responseHandler);


        when(responseHandler.getExceptions())
                .thenReturn(Lists.newArrayList(new IOException("ex 1")))
                .thenReturn(Lists.newArrayList(new SAXException("ex 2")));

        final List<ErrorReport> errorReports = instance
                .downloadResource(objectResource, fileStorageHandle);

        assertThat(errorReports.size(), is(2));
        assertThat(errorReports, containsInAnyOrder(allOf(
                hasProperty("exception", instanceOf(IOException.class))
        ), allOf(
                hasProperty("exception", instanceOf(SAXException.class))
        )));
    }


    private ObjectResource getObjectResource(String url) {
        final ObjectResource objectResource = mock(ObjectResource.class);
        when(objectResource.getXlinkHref()).thenReturn(url);
        return objectResource;
    }


}