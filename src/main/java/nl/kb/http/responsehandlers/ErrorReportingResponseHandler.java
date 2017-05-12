package nl.kb.http.responsehandlers;

import nl.kb.http.HttpResponseException;
import nl.kb.http.HttpResponseHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class ErrorReportingResponseHandler implements HttpResponseHandler {
    final List<SAXException> saxExceptions = new ArrayList<>();
    final List<IOException> ioExceptions = new ArrayList<>();
    private final List<HttpResponseException> httpResponseExceptions = new ArrayList<>();
    private URL url;

    @Override
    public void onResponseError(Integer status, InputStream responseData) {
        final String message = String.format("Url responded with status %d",
                status);

        httpResponseExceptions.add(new HttpResponseException(message, status, url));
    }

    @Override
    public void onRequestError(Exception exception) {

        ioExceptions.add(new IOException(exception.getMessage(), exception));
    }

    @Override
    public void onRedirect(String sourceLocation, String targetLocation) {
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public void throwAnyException() throws IOException, SAXException, HttpResponseException {
        if (ioExceptions.size() > 0) {
            throw ioExceptions.get(0);
        }
        if (saxExceptions.size() > 0) {
            throw saxExceptions.get(0);
        }
        if (httpResponseExceptions.size() > 0) {
            throw httpResponseExceptions.get(0);
        }
    }

    @Override
    public List<Exception> getExceptions() {
        final List<Exception> exceptions = new ArrayList<>();

        exceptions.addAll(ioExceptions);
        exceptions.addAll(saxExceptions);
        exceptions.addAll(httpResponseExceptions);

        return exceptions;
    }
}
