package nl.kb.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class LenientHttpFetcher implements HttpFetcher {

    private final boolean proactivelyClosing;

    public LenientHttpFetcher(boolean proactivelyClosing) {
        this.proactivelyClosing = proactivelyClosing;
    }

    public void execute(URL url, HttpResponseHandler responseHandler) {
        responseHandler.setUrl(url);
        final Optional<HttpURLConnection> connectionOpt = getConnection(url, responseHandler);
        if (!connectionOpt.isPresent()) { return; }

        final HttpURLConnection connection = connectionOpt.get();
        if (proactivelyClosing) {
            connection.setRequestProperty("Connection", "close");
        } else {
            connection.setRequestProperty("Connection", "keep-Alive");
        }
        connection.setInstanceFollowRedirects(false);
        final Optional<Integer> responseCode = getResponseCode(connection, responseHandler);
        if (!responseCode.isPresent()) { return; }

        final Integer statusCode = responseCode.get();

        if (statusCode < 200 || statusCode >= 400) {
            responseHandler.onResponseError(statusCode, null);
            return;
        } else if (statusCode >= 300 && statusCode < 400) {
            final String redirectLocation = connection.getHeaderField("Location") == null ?
                    connection.getHeaderField("location") :
                    connection.getHeaderField("Location") ;
            responseHandler.onRedirect(url.toString(), redirectLocation);

            try {
                final String fixedUrl = CrappyUrlFixingHelper.fixCrappyLocationHeaderValue(url, redirectLocation);
                execute(new URL(fixedUrl), responseHandler);
                return;
            } catch (Exception e) {
                responseHandler.onRequestError(e);
            }
            return;
        }

        final Optional<InputStream> responseDataOpt = getResponseData(connection, responseHandler);
        if (!responseDataOpt.isPresent()) { return; }

        final InputStream responseData = responseDataOpt.get();
        if (statusCode >= 200 && statusCode < 300 ) {
            responseHandler.onResponseData(statusCode, responseData, connection.getHeaderFields());
        }

    }

    private Optional<Integer> getResponseCode(HttpURLConnection connection, HttpResponseHandler responseHandler)  {
        try {
            return Optional.of(connection.getResponseCode());
        } catch (IOException e) {
            responseHandler.onRequestError(e);
            return Optional.empty();
        }
    }

    private Optional<InputStream> getResponseData(HttpURLConnection connection, HttpResponseHandler responseHandler) {
        try {
            final InputStream inputStream = connection.getInputStream();
            return  Optional.of(inputStream);
        } catch (IOException e) {
            responseHandler.onRequestError(e);
            return Optional.empty();
        }
    }

    private Optional<HttpURLConnection> getConnection(URL url, HttpResponseHandler responseHandler) {
        try {
            return Optional.of((HttpURLConnection) url.openConnection());
        } catch (IOException e) {
            responseHandler.onRequestError(e);
            return Optional.empty();
        }
    }
}
