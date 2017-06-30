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
    private static final String VERB = "ListIdentifiers";

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
    private final Consumer<String> onLogMessage;


    private ListIdentifiers(ListIdentifiersBuilder listIdentifiersBuilder) {

        this.oaiUrl = listIdentifiersBuilder.oaiUrl;
        this.oaiSet = listIdentifiersBuilder.oaiSet;
        this.oaiMetadataPrefix = listIdentifiersBuilder.oaiMetadataPrefix;
        this.httpFetcher = listIdentifiersBuilder.httpFetcher;
        this.responseHandlerFactory = listIdentifiersBuilder.responseHandlerFactory;
        this.onHarvestComplete = listIdentifiersBuilder.onHarvestComplete;
        this.onException = listIdentifiersBuilder.onException;
        this.onOaiRecordHeader = listIdentifiersBuilder.onOaiRecordHeader;
        this.oaiDatestamp = listIdentifiersBuilder.oaiDatestamp;
        this.onProgress = listIdentifiersBuilder.onProgress;
        this.lastDateStamp = listIdentifiersBuilder.oaiDatestamp;
        this.onLogMessage = listIdentifiersBuilder.onLogMessage;
    }

    private URL makeRequestUrl(String resumptionToken) throws MalformedURLException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(oaiUrl).append("?").append("verb=").append(VERB);

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


                onLogMessage.accept(String.format("REQUESTING: %s", requestUrl.toString()));

                final long before = System.currentTimeMillis();
                httpFetcher.execute(requestUrl, responseHandler);

                onLogMessage.accept(String.format("RESPONDED: %s (%dms)", requestUrl.toString(),
                        System.currentTimeMillis() -before));

                final Optional<String> optResumptionToken = xmlHandler.getResumptionToken();
                final Optional<String> optDateStamp = xmlHandler.getLastDateStamp();

                if (!responseHandler.getExceptions().isEmpty()) {
                    responseHandler.getExceptions().forEach(onException);
                    break;
                }

                optDateStamp.ifPresent(s -> {
                    lastDateStamp = s;
                    onProgress.accept(lastDateStamp);
                });

                if (optResumptionToken.isPresent()) {
                    resumptionToken = optResumptionToken.get();
                } else {
                    break;
                }
            }

            onHarvestComplete.accept(lastDateStamp);
        } catch (MalformedURLException e) {
            onException.accept(e);
        }
    }

    /**
     * Interrupts the harvest before the next request
     */
    public void interruptHarvest() {
        interrupted = true;
    }

    public static class ListIdentifiersBuilder {
        private String oaiUrl;
        private String oaiSet;
        private String oaiMetadataPrefix;
        private String oaiDatestamp = null;
        private HttpFetcher httpFetcher;
        private ResponseHandlerFactory responseHandlerFactory;
        private Consumer<String> onHarvestComplete = dateStamp -> { /* default failsafe */ };
        private Consumer<Exception> onException = ex -> { /* default failsafe */  };
        private Consumer<OaiRecordHeader> onOaiRecordHeader = record -> { /* default failsafe */   };
        private Consumer<String> onProgress = dateStamp -> { /* default failsafe */  };
        private Consumer<String> onLogMessage = msg -> { /* default failsafe */  };

        public ListIdentifiersBuilder setOaiUrl(String oaiUrl) {
            this.oaiUrl = oaiUrl;
            return this;
        }

        public ListIdentifiersBuilder setOaiSet(String oaiSet) {
            this.oaiSet = oaiSet;
            return this;
        }

        public ListIdentifiersBuilder setOaiMetadataPrefix(String oaiMetadataPrefix) {
            this.oaiMetadataPrefix = oaiMetadataPrefix;
            return this;
        }

        public ListIdentifiersBuilder setOaiDatestamp(String oaiDatestamp) {
            this.oaiDatestamp = oaiDatestamp;
            return this;
        }

        public ListIdentifiersBuilder setHttpFetcher(HttpFetcher httpFetcher) {
            this.httpFetcher = httpFetcher;
            return this;
        }

        public ListIdentifiersBuilder setResponseHandlerFactory(ResponseHandlerFactory responseHandlerFactory) {
            this.responseHandlerFactory = responseHandlerFactory;
            return this;
        }

        public ListIdentifiersBuilder setOnHarvestComplete(Consumer<String> onHarvestComplete) {
            this.onHarvestComplete = onHarvestComplete;
            return this;
        }

        public ListIdentifiersBuilder setOnException(Consumer<Exception> onException) {
            this.onException = onException;
            return this;
        }

        public ListIdentifiersBuilder setOnOaiRecordHeader(Consumer<OaiRecordHeader> onOaiRecordHeader) {
            this.onOaiRecordHeader = onOaiRecordHeader;
            return this;
        }

        public ListIdentifiersBuilder setOnProgress(Consumer<String> onProgress) {
            this.onProgress = onProgress;
            return this;
        }

        public ListIdentifiersBuilder setOnLogMessage(Consumer<String> onLogMessage) {
            this.onLogMessage = onLogMessage;
            return this;
        }

        public ListIdentifiers createListIdentifiers() {
            return new ListIdentifiers(this);
        }
    }
}
