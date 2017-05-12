package nl.kb.stream;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Outpustream that passes bytes written to it into an instance of MessageDigest,
 * in order to generate a checksum.
 */
public class ChecksumOutputStream extends ByteArrayOutputStream {


    private final MessageDigest digest;
    private String checkSumString = null;

    /**
     *
     * @param algorithm String representing the algorithm {@link MessageDigest#getInstance}
     * @throws NoSuchAlgorithmException when algorithm is not supported by {@link MessageDigest#getInstance}
     */
    public ChecksumOutputStream(String algorithm) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm);
    }

    @Override
    public synchronized void write(int b) {
        digest.update((byte) b);
    }

    @Override
    public synchronized void write(byte b[], int off, int len) {
        digest.update(b, off, len);
    }

    /**
     *
     * @return the checksum as byte array
     */
    public byte[] getChecksum() {
        return digest.digest();
    }

    /**
     *
     * @return the checksum as ascii string
     */
    public String getChecksumString() {
        if (checkSumString == null) {
            final StringBuilder sb = new StringBuilder();
            for (byte b : getChecksum()) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            checkSumString = sb.toString();
        }
        return checkSumString;
    }

}
