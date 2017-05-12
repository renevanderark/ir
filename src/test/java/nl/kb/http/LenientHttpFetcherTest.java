package nl.kb.http;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LenientHttpFetcherTest {



    private URL makeUrl(InputStream responseData, HttpURLConnection urlConnection) throws IOException {

        doReturn(responseData).when(urlConnection).getInputStream();

        final URLStreamHandler urlStreamHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return urlConnection;
            }
        };
        return new URL("http", "example.com", 80, "/", urlStreamHandler);
    }



    @Test
    public void executeShouldPassInputStreamAndResponseCodeToHandler() throws IOException {
        final LenientHttpFetcher instance = new LenientHttpFetcher(false);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final InputStream responseData = mock(InputStream.class);
        final HttpURLConnection connection = mock(HttpURLConnection.class);
        final URL url = makeUrl(responseData, connection);
        final Map<String, List<String>> headerFields = new HashMap<>();
        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getHeaderFields()).thenReturn(headerFields);
        instance.execute(url, responseHandler);

        verify(responseHandler).onResponseData(200, responseData, headerFields);
        verify(responseHandler, never()).onRequestError(any());
        verify(responseHandler, never()).onResponseError(any(), any());
        verify(responseHandler, never()).onRedirect(any(), any());
    }

    @Test
    public void executeShouldInvokeSetUrlOfHandler() throws IOException {
        final LenientHttpFetcher instance = new LenientHttpFetcher(false);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final InputStream responseData = mock(InputStream.class);
        final HttpURLConnection connection = mock(HttpURLConnection.class);
        final URL url = makeUrl(responseData, connection);

        instance.execute(url, responseHandler);

        verify(responseHandler).setUrl(url);
    }

    @Test
    public void executeShouldInvokeOnRequestErrorWhenIOExceptionIsThrownByGetResponseCode() throws IOException {
        final LenientHttpFetcher instance = new LenientHttpFetcher(false);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final InputStream responseData = mock(InputStream.class);
        final HttpURLConnection connection = mock(HttpURLConnection.class);
        final URL url = makeUrl(responseData, connection);
        final IOException ioException = new IOException();
        when(connection.getResponseCode()).thenThrow(ioException);

        instance.execute(url, responseHandler);

        verify(responseHandler).onRequestError(ioException);
        verify(responseHandler, never()).onRedirect(any(), any());
        verify(responseHandler, never()).onResponseError(any(), any());
        verify(responseHandler, never()).onResponseData(any(), any(), any());
    }

    @Test
    public void executeShouldInvokeOnResponseErrorWhenIOExceptionIsThrownByGetInputStream() throws IOException {
        final LenientHttpFetcher instance = new LenientHttpFetcher(false);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final InputStream responseData = mock(InputStream.class);
        final HttpURLConnection connection = mock(HttpURLConnection.class);
        final URL url = makeUrl(responseData, connection);
        final IOException ioException = new IOException();
        when(connection.getInputStream()).thenThrow(ioException);

        instance.execute(url, responseHandler);

        verify(responseHandler).onResponseError(any(), any());
        verify(responseHandler, never()).onRedirect(any(), any());
        verify(responseHandler, never()).onRequestError(any());
        verify(responseHandler, never()).onResponseData(any(), any(), any());
    }

    @Test
    public void executeShouldPassInputStreamAndResponseCodeToResponseErrorHandler() throws IOException {
        final LenientHttpFetcher instance = new LenientHttpFetcher(false);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final InputStream responseData = mock(InputStream.class);
        final HttpURLConnection connection = mock(HttpURLConnection.class);
        final URL url = makeUrl(responseData, connection);
        when(connection.getResponseCode()).thenReturn(500);

        instance.execute(url, responseHandler);

        verify(responseHandler).onResponseError(500, null);
        verify(responseHandler, never()).onRequestError(any());
        verify(responseHandler, never()).onResponseData(any(), any(), any());
        verify(responseHandler, never()).onRedirect(any(), any());
    }


    @Test
    public void executeShouldHandleRedirects() throws IOException {
        final LenientHttpFetcher instance = new LenientHttpFetcher(false);
        final HttpResponseHandler responseHandler = mock(HttpResponseHandler.class);
        final InputStream responseData = mock(InputStream.class);
        final HttpURLConnection connection = mock(HttpURLConnection.class);
        final URL url = makeUrl(responseData, connection);
        final String redirectLocation = "<< force fail with invalid url >>";
        when(connection.getResponseCode()).thenReturn(301);
        when(connection.getHeaderField("Location")).thenReturn(redirectLocation);

        instance.execute(url, responseHandler);

        verify(responseHandler).onRedirect("http://example.com:80/", redirectLocation);
        verify(responseHandler).onRequestError(any(Exception.class));
        verify(responseHandler, never()).onResponseError(any(), any());
        verify(responseHandler, never()).onResponseData(any(), any(), any());
    }

}