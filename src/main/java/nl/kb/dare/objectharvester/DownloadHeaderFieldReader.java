package nl.kb.dare.objectharvester;

import nl.kb.http.HeaderConsumer;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class DownloadHeaderFieldReader extends ByteArrayOutputStream implements HeaderConsumer {
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_TYPE = "Content-Type";

    private String contentDisposition = "";
    private String contentType = "";

    @Override
    public synchronized void write(int b) {

        /* leave unimplemented */
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {

        /* leave unimplemented */
    }


    @Override
    public void consumeHeaders(Map<String, List<String>> headerFields) {
        if (headerFields.containsKey(CONTENT_DISPOSITION) && !headerFields.get(CONTENT_DISPOSITION).isEmpty()) {
            contentDisposition = headerFields.get(CONTENT_DISPOSITION).get(0);
        }

        if (headerFields.containsKey(CONTENT_TYPE) && !headerFields.get(CONTENT_TYPE).isEmpty()) {
            contentType = headerFields.get(CONTENT_TYPE).get(0);
        }

    }

    String getContentDisposition() {
        return contentDisposition;
    }

    String getContentType() {
        return contentType;
    }
}
