package nl.kb.http.responsehandlers;

import nl.kb.http.HttpResponseHandler;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class StreamCopyingResponseHandlerTest {

    @Test
    public void itShouldCopyTheBytesToTheOutputStreams() {
        final ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        final ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        final ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        final HttpResponseHandler instance = new ResponseHandlerFactory().getStreamCopyingResponseHandler(out1, out2, out3);

        instance.onResponseData(200, StreamCopyingResponseHandler.class.getResourceAsStream("/http/text.txt"), null);

        assertThat(new String(out1.toByteArray(), Charset.forName("UTF8")), is("testing"));
        assertThat(new String(out2.toByteArray(), Charset.forName("UTF8")), is("testing"));
        assertThat(new String(out3.toByteArray(), Charset.forName("UTF8")), is("testing"));
    }

    @Test
    public void itShouldLogIOExceptions() throws IOException {
        final OutputStream out = mock(OutputStream.class);
        final ByteArrayOutputStream checksumOut = new ByteArrayOutputStream();
        final HttpResponseHandler instance = new ResponseHandlerFactory().getStreamCopyingResponseHandler(out, checksumOut);

        doThrow(IOException.class).when(out).write(any(), anyInt(), anyInt());

        instance.onResponseData(200, StreamCopyingResponseHandler.class.getResourceAsStream("/http/text.txt"), null);

        assertThat(instance.getExceptions().size(), is(1));
    }
}