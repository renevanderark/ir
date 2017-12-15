package nl.kb.http;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LenientHttpFetcher implements HttpFetcher, Monitable {

    private final boolean proactivelyClosing;
    private final int connectTimeout;
    private final int readTimeout;

    private final Set<ConnectionMonit> openConnections = new ConcurrentHashSet<>();

    public LenientHttpFetcher(boolean proactivelyClosing) {
        this(proactivelyClosing, 300_000);
    }

    LenientHttpFetcher(boolean proactivelyClosing, int connectTimeout) {
        this(proactivelyClosing, connectTimeout, 300_000);
    }

    LenientHttpFetcher(boolean proactivelyClosing, int connectTimeout, int readTimeout) {
        this.proactivelyClosing = proactivelyClosing;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }


    public List<ConnectionMonit> getOpenConnections() {
        return openConnections.stream()
                .sorted((a, b) -> (int) (b.getOpenTime() - a.getOpenTime()))
                .collect(Collectors.toList());
    }

    @Override
    public void disconnectStalledConnections(int maximumDownloadStallTimeMs) {
        openConnections.stream()
            .filter(x -> x.getOpenTime() > maximumDownloadStallTimeMs)
            .forEach(connectionMonit -> {
                connectionMonit.getResponseHandler().onRequestError(
                    new IOException("Download time exceeded maximum stall time: " + maximumDownloadStallTimeMs + "ms"));

                final HttpURLConnection urlConnection = connectionMonit.getUrlConnection();
                final InputStream inputStream = connectionMonit.getInputStream();
                if (urlConnection != null) {
                    try {
                        urlConnection.disconnect();
                    } catch (Exception e) {
                        // force disconnect
                    }
                }

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        // force close
                    }
                }

            });
    }


    public void execute(URL url, HttpResponseHandler responseHandler) {
        responseHandler.setUrl(url);
        final Optional<HttpURLConnection> connectionOpt = getConnection(url, responseHandler);
        if (!connectionOpt.isPresent()) {
            return;
        }

        final HttpURLConnection connection = connectionOpt.get();
        final ConnectionMonit connectionMonit = new ConnectionMonit(connection, responseHandler);
        openConnections.add(connectionMonit);

        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        if (proactivelyClosing) {
            connection.setRequestProperty("Connection", "close");
        } else {
            connection.setRequestProperty("Connection", "keep-Alive");
        }
        connection.setInstanceFollowRedirects(false);
        final Optional<Integer> responseCode = getResponseCode(connection, responseHandler);
        if (!responseCode.isPresent()) {
            openConnections.remove(connectionMonit);
            return;
        }

        final Integer statusCode = responseCode.get();

        if (statusCode < 200 || statusCode >= 400) {
            responseHandler.onResponseError(statusCode, null);

            openConnections.remove(connectionMonit);
            return;

        } else if (statusCode >= 300 && statusCode < 400) {
            final String redirectLocation = connection.getHeaderField("Location") == null ?
                    connection.getHeaderField("location") :
                    connection.getHeaderField("Location");
            responseHandler.onRedirect(url.toString(), redirectLocation);
            openConnections.remove(connectionMonit);

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
        if (!responseDataOpt.isPresent()) {
            openConnections.remove(connectionMonit);
            return;
        }

        final InputStream responseData = responseDataOpt.get();
        connectionMonit.setInputStream(responseData);
        if (statusCode >= 200 && statusCode < 300) {
            responseHandler.onResponseData(statusCode, responseData, connection.getHeaderFields());
            openConnections.remove(connectionMonit);
        }
    }


    private Optional<Integer> getResponseCode(HttpURLConnection connection, HttpResponseHandler responseHandler) {
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
            return Optional.of(inputStream);
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
