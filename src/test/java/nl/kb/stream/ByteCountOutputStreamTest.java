package nl.kb.stream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ByteCountOutputStreamTest {

    @Test
    public void itShouldCountTheBytes() throws IOException, NoSuchAlgorithmException {
        final ByteCountOutputStream instance = new ByteCountOutputStream();
        final InputStream in = ChecksumOutputStreamTest.class.getResourceAsStream("/http/text.txt");

        IOUtils.copy(in, instance);

        assertThat(instance.getCurrentByteCount(), is(7L));
    }
}