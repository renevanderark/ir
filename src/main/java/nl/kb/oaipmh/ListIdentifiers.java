package nl.kb.oaipmh;

import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseHandler;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Harvester for the ListIdentifiers verb
 */
public class ListIdentifiers {

    private final String oaiUrl;
    private final String oaiSet;
    private final String oaiMetadataPrefix;
    private final String oaiDatestamp;

    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final Consumer<String> onHarvestComplete;
    private final Consumer<Exception> onException;
    private final Consumer<OaiRecordHeader> onOaiRecordHeader;
    private final Consumer<String> onProgress;

    private boolean interrupted = false;
    private String lastDateStamp;
    private String verb = "ListIdentifiers";


    /**
     * Constructor is meant for a single harvest, either from start, or from oaiDatestamp
     * @param oaiUrl the endpoint
     * @param oaiSet the set
     * @param oaiMetadataPrefix the metadataPrefix
     * @param oaiDatestamp the date stamp that will be used for the from parameter (i.e. last record from last harvest)
     * @param httpFetcher the implementor of the GET requests
     * @param responseHandlerFactory the provider of the ResponseHandler
     * @param onHarvestComplete callback invoked after harvest with the latest datestamp encountered
     * @param onException callback invoked when encountering exceptions during harvest
     * @param onOaiRecordHeader callback invoked for each encountered OAI/PMH record header
     * @param onProgress callback invoked for each new request to the endpoint
     */
    public ListIdentifiers(
            String oaiUrl,
            String oaiSet,
            String oaiMetadataPrefix,
            String oaiDatestamp,
            HttpFetcher httpFetcher,
            ResponseHandlerFactory responseHandlerFactory,
            Consumer<String> onHarvestComplete,
            Consumer<Exception> onException,
            Consumer<OaiRecordHeader> onOaiRecordHeader,
            Consumer<String> onProgress) {

        this.oaiUrl = oaiUrl;
        this.oaiSet = oaiSet;
        this.oaiMetadataPrefix = oaiMetadataPrefix;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.onHarvestComplete = onHarvestComplete;
        this.onException = onException;
        this.onOaiRecordHeader = onOaiRecordHeader;
        this.oaiDatestamp = oaiDatestamp;
        this.onProgress = onProgress;

        lastDateStamp = oaiDatestamp;
    }

    private URL makeRequestUrl(String resumptionToken) throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(oaiUrl).append("?").append("verb=").append(verb);

        if (resumptionToken != null) {
            urlBuilder.append("&").append(String.format("resumptionToken=%s", resumptionToken));
        } else {
            urlBuilder
                    .append("&").append(String.format("set=%s", oaiSet))
                    .append("&").append(String.format("metadataPrefix=%s", oaiMetadataPrefix));

            if (oaiDatestamp != null) {
                urlBuilder.append("&").append(String.format("from=%s", oaiDatestamp));
            }
        }
        return new URL(urlBuilder.toString());
    }

    /**
     * Performs harvest for verb=ListIdentifiers
     */
    public void harvest() {
        try {

            String resumptionToken = null;
            lastDateStamp = oaiDatestamp;

            while (!interrupted && (resumptionToken == null || resumptionToken.trim().length() > 0)) {
                final ListIdentifiersXmlHandler xmlHandler = ListIdentifiersXmlHandler.getNewInstance(onOaiRecordHeader);
                final HttpResponseHandler responseHandler = responseHandlerFactory.getSaxParsingHandler(xmlHandler);
                final URL requestUrl = makeRequestUrl(resumptionToken);

                onProgress.accept(requestUrl.toString());

                httpFetcher.execute(requestUrl, responseHandler);
                final Optional<String> optResumptionToken = xmlHandler.getResumptionToken();
                final Optional<String> optDateStamp = xmlHandler.getLastDateStamp();

                if (responseHandler.getExceptions().size() > 0) {
                    responseHandler.getExceptions().forEach(onException);
                    break;
                }

                optDateStamp.ifPresent(s -> lastDateStamp = s);

                if (optResumptionToken.isPresent()) {
                    resumptionToken = optResumptionToken.get();
                } else {
                    break;
                }
            }

            onHarvestComplete.accept(lastDateStamp);
        } catch (MalformedURLException e) {
            // SEVERE!!
            throw new RuntimeException(e);
        }
    }

    /**
     * Interrupts the harvest before the next request
     */
    public void interruptHarvest() {
        interrupted = true;
    }

    public ListIdentifiers setVerb(String verb) {
        this.verb = verb;
        return this;
    }
}
