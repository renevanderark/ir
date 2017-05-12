package nl.kb.http.responsehandlers;

import nl.kb.http.HttpResponseException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

public class ErrorReportingResponseHandlerTest {

    private ErrorReportingResponseHandler instance;
    private static final URL THE_URL;
    static {
        try {
            THE_URL = new URL("http://example.com");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setup() throws MalformedURLException {
        instance = new ErrorReportingResponseHandler() {

            @Override
            public void onResponseData(Integer status, InputStream responseData, Map<String, List<String>> headerFields) {

            }
        };

        instance.setUrl(THE_URL);
    }

    @Test
    public void onResponseErrorShouldAddAnHttpResponseErrorToTheListOfExceptions()  {
        instance.onResponseError(400, mock(InputStream.class));
        instance.onResponseError(500, mock(InputStream.class));

        assertThat(instance.getExceptions(), contains(
            allOf(
                instanceOf(HttpResponseException.class),
                hasProperty("url", is(THE_URL)),
                hasProperty("statusCode", is(400))
            ), allOf(
                instanceOf(HttpResponseException.class),
                hasProperty("url", is(THE_URL)),
                hasProperty("statusCode", is(500))
            )
        ));
    }

    @Test
    public void onRequestErrorShouldAddAnIoExceptionToTheListOfExceptions()  {
        instance.onRequestError(new IOException("problem"));
        instance.onRequestError(new SAXException("another problem"));

        assertThat(instance.getExceptions(), containsInAnyOrder(
                allOf(
                    instanceOf(IOException.class),
                    hasProperty("message", is("problem"))
                ), allOf(
                    instanceOf(IOException.class),
                    hasProperty("message", is("another problem"))
                )
        ));
    }

    @Test
    public void onRedirectShouldMaybeDoSomethingMaybeNot()  {
        instance.onRedirect("from", "to");
        // TODO ?
    }

    @Test(expected = Exception.class)
    public void throwAnyExceptionShouldThrowAnExceptionFromTheListOfExceptions() throws IOException, SAXException, HttpResponseException {
        instance.onRequestError(new IOException("problem"));
        instance.onResponseError(500, mock(InputStream.class));

        instance.throwAnyException();
    }

    @Test
    public void getExceptionsShouldReturnTheListOfExceptions()  {
        instance.onResponseError(400, mock(InputStream.class));
        instance.onResponseError(500, mock(InputStream.class));
        instance.onRequestError(new IOException("problem"));
        instance.onRequestError(new SAXException("another problem"));

        assertThat(instance.getExceptions(), containsInAnyOrder(
            allOf(
                    instanceOf(HttpResponseException.class),
                    hasProperty("url", is(THE_URL)),
                    hasProperty("statusCode", is(400))
            ), allOf(
                    instanceOf(HttpResponseException.class),
                    hasProperty("url", is(THE_URL)),
                    hasProperty("statusCode", is(500))
            ), allOf(
                    instanceOf(IOException.class)
            ), allOf(
                    instanceOf(IOException.class)
            )
        ));
    }

}