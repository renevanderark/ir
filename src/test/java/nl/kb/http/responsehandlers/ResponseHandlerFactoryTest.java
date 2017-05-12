package nl.kb.http.responsehandlers;

import nl.kb.http.HttpResponseHandler;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.helpers.DefaultHandler;

import java.io.OutputStream;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ResponseHandlerFactoryTest {

    private ResponseHandlerFactory instance;

    @Before
    public void setup() {
        instance = new ResponseHandlerFactory();
    }

    @Test
    public void getSaxParsingHandlerShouldReturnSaxParsingHandler() {
        final HttpResponseHandler handler = instance.getSaxParsingHandler(new DefaultHandler());

        assertThat(handler, is(instanceOf(SaxParsingResponseHandler.class)));
    }

    @Test
    public void getStreamCopyingResponseHandlerShouldReturnStreamCopyingHandler() {
        final HttpResponseHandler handler = instance.getStreamCopyingResponseHandler(mock(OutputStream.class), mock(OutputStream.class));

        assertThat(handler, is(instanceOf(StreamCopyingResponseHandler.class)));
    }

}