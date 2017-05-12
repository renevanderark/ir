package nl.kb.http;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface HttpResponseHandler {

    void onResponseData(Integer status, InputStream responseData, Map<String, List<String>> headerFields);

    void onResponseError(Integer status, InputStream responseData);

    void onRequestError(Exception exception);

    void onRedirect(String sourceLocation, String targetLocation);

    void setUrl(URL url);

    void throwAnyException() throws IOException, SAXException, HttpResponseException;

    List<Exception> getExceptions();
}
