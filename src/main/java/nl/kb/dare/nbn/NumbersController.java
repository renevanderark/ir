package nl.kb.dare.nbn;

import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseHandler;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class NumbersController {
    private static final Logger LOG = LoggerFactory.getLogger(NumbersController.class);

    private final String numbersEndpoint;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    public NumbersController(String numbersEndpoint, HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.numbersEndpoint = numbersEndpoint;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    public List<Long> getNumbers(int quantity) {
        try {
            final NumbersXmlHandler numbersHandler = new NumbersXmlHandler();
            final HttpResponseHandler responseHandler = responseHandlerFactory.getSaxParsingHandler(numbersHandler);
            List<Exception> exceptions = null;

            while (exceptions == null || exceptions.size() > 0) {
                httpFetcher.execute(new URL(String.format("%s?qt=%d", numbersEndpoint, quantity)),
                        responseHandler);
                exceptions = responseHandler.getExceptions();

                if (exceptions.size() > 0) {
                    LOG.error("Failed to reach number generator at: {}", numbersEndpoint, exceptions.get(0));
                    LOG.warn("Will retry infinitely to reach number generator so please fix it!");
                    try { Thread.sleep(5000); } catch (InterruptedException ignored) { }
                }
            }
            return numbersHandler.getResult();

        } catch (MalformedURLException e) {
            throw new RuntimeException("SEVERE, malformed url to numbers endpoint: " + numbersEndpoint);
        }
    }
}
