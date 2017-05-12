package nl.kb.http.responsehandlers;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class SaxParsingResponseHandlerTest {

    private InputStream xml;

    @Test
    public void itShouldParseTheDataWithTheXmlHandler() throws SAXException {
        final ByteArrayInputStream input = new ByteArrayInputStream("<valid></valid>".getBytes(StandardCharsets.UTF_8));
        final DefaultHandler xmlHandler = mock(DefaultHandler.class);
        final SaxParsingResponseHandler instance = new SaxParsingResponseHandler(xmlHandler);

        instance.onResponseData(200, input, null);

        verify(xmlHandler).startElement(anyString(), anyString(), argThat(is("valid")), any());
        verify(xmlHandler).endElement(anyString(), anyString(), argThat(is("valid")));
    }

    @Test
    public void itShouldLogSaxExceptions() {
        final ByteArrayInputStream input = new ByteArrayInputStream("<valid></invali".getBytes(StandardCharsets.UTF_8));
        final DefaultHandler xmlHandler = mock(DefaultHandler.class);
        final SaxParsingResponseHandler instance = new SaxParsingResponseHandler(xmlHandler);

        instance.onResponseData(200, input, null);

        assertThat(instance.getExceptions().isEmpty(), is(false));
        assertThat(instance.getExceptions().get(0), is(instanceOf(SAXException.class)));
    }

    @Test
    public void itShouldLogIOExceptions() throws IOException {
        final InputStream input = mock(InputStream.class);
        final DefaultHandler xmlHandler = mock(DefaultHandler.class);
        final SaxParsingResponseHandler instance = new SaxParsingResponseHandler(xmlHandler);

        doThrow(IOException.class).when(input).read(any());
        doThrow(IOException.class).when(input).read(any(), anyInt(), anyInt());
        doThrow(IOException.class).when(input).read();

        instance.onResponseData(200, input, null);

        assertThat(instance.getExceptions().isEmpty(), is(false));
        assertThat(instance.getExceptions().get(0), is(instanceOf(IOException.class)));
    }

}