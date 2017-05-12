package nl.kb.stream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ChecksumOutputStreamTest {

    @Test
    public void itShouldWriteAChecksum() throws IOException, NoSuchAlgorithmException {
        final ChecksumOutputStream instance = new ChecksumOutputStream("SHA-512");
        final InputStream in = ChecksumOutputStreamTest.class.getResourceAsStream("/http/text.txt");

        IOUtils.copy(in, instance);

        assertThat(instance.getChecksumString(), is("521b9ccefbcd14d179e7a1bb877752870a6d620938b28a66a107eac6e6805b9d0989f45b5730508041aa5e710847d439ea74cd312c9355f1f2dae08d40e41d50"));
    }
}