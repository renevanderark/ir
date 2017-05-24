package nl.kb.dare.model.repository;


import nl.kb.dare.model.RunState;
import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseException;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RepositoryValidatorTest {

    private InputStream mdFormatsXml;
    private InputStream listSetsXml;
    private InputStream corruptXml;

    @Before
    public void setup() {
        mdFormatsXml = RepositoryValidatorTest.class.getResourceAsStream("/model/repository/ListMetadataFormats.xml");
        listSetsXml = RepositoryValidatorTest.class.getResourceAsStream("/model/repository/ListSets.xml");
        corruptXml = new ByteArrayInputStream("<invalid></".getBytes(StandardCharsets.UTF_8));
    }

    private HttpFetcher getMockHttpFetcher(InputStream firstExpected, InputStream secondExpected) {
        return new HttpFetcher() {
            private int count = 0;

            @Override
            public void execute(URL url, HttpResponseHandler responseHandler) {
                if (count == 0) {
                    responseHandler.onResponseData(200, firstExpected, null);
                } else {
                    responseHandler.onResponseData(200, secondExpected, null);
                }
                count++;
            }
        };
    }

    @Test
    public void validateShouldSucceedWhetherRepositoryConfigIsSupportedByEndpoint() throws Exception {
        final HttpFetcher mockHttpFetcher = getMockHttpFetcher(listSetsXml, mdFormatsXml);
        final RepositoryValidator instance = new RepositoryValidator(mockHttpFetcher, new ResponseHandlerFactory());
        final Repository validConfig = new Repository("http://example.com", "name", "nl_didl_norm", "uvt:withfulltext:yes", null, true, HarvestSchedule.DAILY, RunState.WAITING);

        final RepositoryValidator.ValidationResult validationResult = instance.validate(validConfig);

        assertThat(validationResult.metadataFormatSupported, is(true));
        assertThat(validationResult.setExists, is(true));
    }

    @Test
    public void validateShouldFailWhetherRepositoryConfigIsSupportedByEndpoint() throws Exception {
        final HttpFetcher mockHttpFetcher = getMockHttpFetcher(listSetsXml, mdFormatsXml);
        final RepositoryValidator instance = new RepositoryValidator(mockHttpFetcher, new ResponseHandlerFactory());
        final Repository validConfig = new Repository("http://example.com", "name", "unsupported_Md", "nonexistent_set", null, true, HarvestSchedule.DAILY, RunState.WAITING);

        final RepositoryValidator.ValidationResult validationResult = instance.validate(validConfig);

        assertThat(validationResult.metadataFormatSupported, is(false));
        assertThat(validationResult.setExists, is(false));
    }

    @Test(expected = SAXException.class)
    public void validateShouldThrowWhenXmlParsingFails() throws IOException, SAXException, HttpResponseException {
        final HttpFetcher mockHttpFetcher = getMockHttpFetcher(corruptXml, mdFormatsXml);
        final RepositoryValidator instance = new RepositoryValidator(mockHttpFetcher, new ResponseHandlerFactory());
        final Repository validConfig = new Repository("http://example.com", "name", "nl_didl_norm", "uvt:withfulltext:yes", null, true, HarvestSchedule.DAILY, RunState.WAITING);

        instance.validate(validConfig);
    }

    @Test(expected = HttpResponseException.class)
    public void validateShouldThrowWhenHttpRequestFails() throws IOException, SAXException, HttpResponseException {
        final HttpFetcher failingFetcher = (url, responseHandler) -> responseHandler.onRequestError(new Exception("fails"));
        final Repository validConfig = new Repository("http://example.com", "name", "nl_didl_norm", "uvt:withfulltext:yes", null, true, HarvestSchedule.DAILY, RunState.WAITING);
        final RepositoryValidator instance = new RepositoryValidator(failingFetcher, new ResponseHandlerFactory());

        instance.validate(validConfig);
    }

    @After
    public void tearDown() {
        try {
            mdFormatsXml.close();
            listSetsXml.close();
            corruptXml.close();
        } catch (IOException ignored) {

        }
    }
}