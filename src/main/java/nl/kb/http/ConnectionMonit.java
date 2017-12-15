package nl.kb.http;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class ConnectionMonit {
    private final long startTime;
    private final HttpURLConnection urlConnection;
    private final HttpResponseHandler responseHandler;
    private InputStream inputStream;

    ConnectionMonit(HttpURLConnection urlConnection, HttpResponseHandler responseHandler) {
        this.responseHandler = responseHandler;

        this.startTime = System.currentTimeMillis();
        this.urlConnection = urlConnection;
    }

    public long getOpenTime() {
        return System.currentTimeMillis() - startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getUrl() {
        return urlConnection.getURL().toString();
    }

    HttpURLConnection getUrlConnection() {
        return urlConnection;
    }

    InputStream getInputStream() {
        return inputStream;
    }

    HttpResponseHandler getResponseHandler() {
        return responseHandler;
    }

    void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
