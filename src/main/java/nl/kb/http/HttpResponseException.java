package nl.kb.http;

import java.net.URL;

public class HttpResponseException extends Exception {

    private final Integer statusCode;
    private final URL url;

    public HttpResponseException(String message, int errorStatus, URL url) {
        super(message);

        this.statusCode = errorStatus;
        this.url = url;
    }


    public int getStatusCode() {
        return statusCode;
    }

    public URL getUrl() {
        return url;
    }
}
