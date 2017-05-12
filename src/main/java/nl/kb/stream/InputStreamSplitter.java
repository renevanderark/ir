package nl.kb.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Used to split one input stream into multiple outputstreams
 */
public class InputStreamSplitter {

    private final InputStream inputStream;
    private final List<OutputStream> outputStreams;

    public InputStreamSplitter(InputStream inputStream, List<OutputStream> outputStreams) {
        this.inputStream = inputStream;
        this.outputStreams = outputStreams;
    }

    public InputStreamSplitter(InputStream inputStream, OutputStream... outputStreams) {
        this(inputStream, Arrays.asList(outputStreams));
    }

    /**
     * Reads the entire input stream in chunks and copies the chunks to the output streams
     * Proactively closes all the streams when done.
     * @throws IOException
     */
    public void copy() throws IOException {
        byte[] buffer = new byte[1024];
        int numRead;
        do {
            numRead = inputStream.read(buffer);
            if (numRead > 0) {
                for (OutputStream outputStream : outputStreams) {
                    outputStream.write(buffer, 0, numRead);
                }

            }
        } while (numRead != -1);
        inputStream.close();

        for (OutputStream outputStream : outputStreams) {
            outputStream.close();
        }
    }
}
