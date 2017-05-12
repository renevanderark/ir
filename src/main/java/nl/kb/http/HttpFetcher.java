package nl.kb.http;

import java.net.URL;

public interface HttpFetcher {
    void execute(URL url, HttpResponseHandler responseHandler);
}
