package nl.kb.dare.model.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseException;
import nl.kb.http.HttpResponseHandler;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.URL;

public class RepositoryValidator {
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    public class ValidationResult {
        @JsonProperty
        Boolean setExists = false;
        @JsonProperty
        Boolean metadataFormatSupported = false;
    }

    public RepositoryValidator(HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    public ValidationResult validate(Repository repositoryConfig) throws IOException, SAXException, HttpResponseException {
        final URL listSetsUrl = new URL(String.format("%s?verb=ListSets", repositoryConfig.getUrl()));
        final URL listMdUrl = new URL(String.format("%s?verb=ListMetadataFormats", repositoryConfig.getUrl()));

        final ValidationResult validationResult = new ValidationResult();

        final ListSetsXmlHandler listSetsXmlHandler = new ListSetsXmlHandler(repositoryConfig.getSet(), validationResult);
        final ListMetadataFormatsXmlHandler listMetadataFormatsXmlHandler =
                new ListMetadataFormatsXmlHandler(repositoryConfig.getMetadataPrefix(), validationResult);

        final HttpResponseHandler listSetsHandler = responseHandlerFactory.getSaxParsingHandler(listSetsXmlHandler);
        httpFetcher.execute(listSetsUrl, listSetsHandler);
        listSetsHandler.throwAnyException();

        final HttpResponseHandler listMdHandler = responseHandlerFactory.getSaxParsingHandler(listMetadataFormatsXmlHandler);
        httpFetcher.execute(listMdUrl, listMdHandler);
        listMdHandler.throwAnyException();

        return validationResult;
    }

    private static class ListSetsXmlHandler extends DefaultHandler {
        private final String SET_SPEC = "setSpec";
        private final String expectedSet;
        private ValidationResult validationResult;

        private boolean inSetSpec = false;

        ListSetsXmlHandler(String expectedSet, ValidationResult validationResult) {
            this.expectedSet = expectedSet;
            this.validationResult = validationResult;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equalsIgnoreCase(SET_SPEC)) {
                inSetSpec = true;
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            if (inSetSpec && new String(ch, start, length).trim().equalsIgnoreCase(expectedSet)) {
                validationResult.setExists = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equalsIgnoreCase(SET_SPEC)) {
                inSetSpec = false;
            }
        }
    }

    private static class ListMetadataFormatsXmlHandler extends DefaultHandler {

        private static final String METADATA_PREFIX = "metadataPrefix";
        private final String expectedMetadataPrefix;
        private final ValidationResult validationResult;
        private boolean inMetadataPrefix = false;

        ListMetadataFormatsXmlHandler(String expectedMetadataPrefix, ValidationResult validationResult) {
            this.expectedMetadataPrefix = expectedMetadataPrefix;
            this.validationResult = validationResult;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equalsIgnoreCase(METADATA_PREFIX)) {
                inMetadataPrefix = true;
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            if (inMetadataPrefix && new String(ch, start, length).trim().equalsIgnoreCase(expectedMetadataPrefix)) {
                validationResult.metadataFormatSupported = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (qName.equalsIgnoreCase(METADATA_PREFIX)) {
                inMetadataPrefix = false;
            }
        }
    }

}
