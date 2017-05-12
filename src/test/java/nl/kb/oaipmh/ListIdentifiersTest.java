package nl.kb.oaipmh;

import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseHandler;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ListIdentifiersTest {

    private class MockHttpFetcher implements HttpFetcher {

        int count = 0;
        private final InputStream[] mockResponses;

        MockHttpFetcher(InputStream... mockResponses) {
            this.mockResponses = mockResponses;
        }

        @Override
        public void execute(URL url, HttpResponseHandler responseHandler) {
            responseHandler.onResponseData(202, mockResponses[count++], null);
        }

    }

    private InputStream withResumptionToken;
    private InputStream withoutResumptionToken;
    private InputStream withResumptionToken2;
    private InputStream corruptXml;


    @Before
    public void setup() {

        withResumptionToken = ListIdentifiersTest.class.getResourceAsStream("/oai/ListIdentifiersWithResumptionToken.xml");
        withResumptionToken2 = ListIdentifiersTest.class.getResourceAsStream("/oai/ListIdentifiersWithResumptionToken.xml");
        withoutResumptionToken = ListIdentifiersTest.class.getResourceAsStream("/oai/ListIdentifiersWithoutResumptionToken.xml");
        corruptXml = new ByteArrayInputStream("<invalid></".getBytes(StandardCharsets.UTF_8));
    }


    @After
    public void tearDown() {
        try {
            withResumptionToken.close();
            withResumptionToken2.close();
            withoutResumptionToken.close();
            corruptXml.close();
        } catch (IOException ignored) {

        }
    }

    @Test
    public void harvestShouldHarvestUntilThereAreNoMoreResumptionTokens() {
        final MockHttpFetcher httpFetcher = new MockHttpFetcher(withResumptionToken, withoutResumptionToken);
        final Consumer<String> datestampConsumer = (datestamp) -> { };
        final Consumer<Exception> errorConsumer = (err) -> { };
        final Consumer<OaiRecordHeader> onOaiRecordHeader = (oaiRecord) -> { };
        final Consumer<String> onProgress = (str) -> { };
        final ListIdentifiers instance = new ListIdentifiers(
                "http://oai-endpoint.org",
                "setName",
                "md:pref",
                null,
                httpFetcher,
                new ResponseHandlerFactory(),
                datestampConsumer,
                errorConsumer,
                onOaiRecordHeader,
                onProgress);

        instance.harvest();

        assertThat(httpFetcher.count, is(2));
    }

    @Test
    public void harvestShouldInvokeOnHarvestCompleteOnceWithRepoSetToLatestDatestampFromLastHarvestResponse() {
        final List<String> dateStamps = new ArrayList<>();
        final MockHttpFetcher httpFetcher = new MockHttpFetcher(withResumptionToken, withoutResumptionToken);
        final Consumer<String> datestampConsumer = dateStamps::add;
        final Consumer<Exception> errorConsumer = (err) -> { };
        final Consumer<OaiRecordHeader> onOaiRecordHeader = (oaiRecord) -> { };
        final Consumer<String> onProgress = (str) -> { };
        final ListIdentifiers instance = new ListIdentifiers(
                "http://oai-endpoint.org",
                "setName",
                "md:pref",
                null,
                httpFetcher,
                new ResponseHandlerFactory(),
                datestampConsumer,
                errorConsumer,
                onOaiRecordHeader,
                onProgress);


        instance.harvest();

        assertThat(dateStamps.size(), is(1));
        // Value taken from last record in ListIdentifiersWithoutResumptionToken.xml
        assertThat(dateStamps.get(0), is("2017-01-18T01:00:31Z"));
    }

    @Test
    public void harvestShouldLogErrorAndTerminateAfterLastSuccesfulResponse() {
        final String orignalDateStamp = "initialDatestampValue";
        final MockHttpFetcher httpFetcher = new MockHttpFetcher(corruptXml);
        final List<String> dateStamps = new ArrayList<>();
        final List<Exception> exceptions = new ArrayList<>();
        final Consumer<String> datestampConsumer = dateStamps::add;
        final Consumer<Exception> errorConsumer = exceptions::add;
        final Consumer<OaiRecordHeader> onOaiRecordHeader = (oaiRecord) -> { };
        final Consumer<String> onProgress = (str) -> { };
        final ListIdentifiers instance = new ListIdentifiers(
                "http://oai-endpoint.org",
                "setName",
                "md:pref",
                orignalDateStamp,
                httpFetcher,
                new ResponseHandlerFactory(),
                datestampConsumer,
                errorConsumer,
                onOaiRecordHeader,
                onProgress);


        instance.harvest();

        assertThat(exceptions.size(), is(1));
        assertThat(exceptions.get(0), instanceOf(SAXException.class));

        assertThat(dateStamps.size(), is(1));
        // Original value
        assertThat(dateStamps.get(0), is(orignalDateStamp));
    }

    @Test
    public void harvestShouldInvokeOnOaiRecordConsumerWithTheOaiRecord() {
        final List<OaiRecordHeader> oaiRecords = new ArrayList<>();
        final String orignalDateStamp = "initialDatestampValue";
        final MockHttpFetcher httpFetcher = new MockHttpFetcher(withResumptionToken, withoutResumptionToken);
        final Consumer<String> datestampConsumer = (datestamp) -> { };
        final Consumer<Exception> errorConsumer = (err) -> { };
        final Consumer<OaiRecordHeader> onOaiRecordHeader = oaiRecords::add;
        final Consumer<String> onProgress = (str) -> { };
        final ListIdentifiers instance = new ListIdentifiers(
                "http://oai-endpoint.org",
                "setName",
                "md:pref",
                orignalDateStamp,
                httpFetcher,
                new ResponseHandlerFactory(),
                datestampConsumer,
                errorConsumer,
                onOaiRecordHeader,
                onProgress);

        instance.harvest();

        assertThat(oaiRecords.size(), is(5));

        // Value taken from first record in ListIdentifiersWithResumptionToken.xml
        assertThat(oaiRecords.get(0), allOf(
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/162830")),
            hasProperty("dateStamp", is("2017-01-13T01:05:49Z")),
            hasProperty("oaiStatus", is(OaiStatus.AVAILABLE))
        ));

        // Value taken from second record in ListIdentifiersWithResumptionToken.xml
        assertThat(oaiRecords.get(1), allOf(
            hasProperty("oaiStatus", is(OaiStatus.DELETED)),
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/162859"))
        ));

        // Value taken from last record in ListIdentifiersWithoutResumptionToken.xml
        assertThat(oaiRecords.get(4), allOf(
            hasProperty("identifier", is("ru:oai:repository.ubn.ru.nl:2066/161841")),
            hasProperty("dateStamp", is("2017-01-18T01:00:31Z")),
            hasProperty("oaiStatus", is(OaiStatus.AVAILABLE))
        ));

    }
}