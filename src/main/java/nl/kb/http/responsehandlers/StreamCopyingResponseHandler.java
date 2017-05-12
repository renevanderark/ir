package nl.kb.http.responsehandlers;

import nl.kb.http.HeaderConsumer;
import nl.kb.stream.InputStreamSplitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class StreamCopyingResponseHandler extends ErrorReportingResponseHandler {

    private final List<OutputStream> outputStreams;

    StreamCopyingResponseHandler(OutputStream... outputStreams) {
        this.outputStreams = Arrays.asList(outputStreams);
    }

    @Override
    public void onResponseData(Integer status, InputStream responseData, Map<String, List<String>> headerFields) {
        try {
            outputStreams
                    .stream()
                    .filter(out -> out instanceof HeaderConsumer)
                    .forEach(out -> ((HeaderConsumer) out).consumeHeaders(headerFields));

            InputStreamSplitter inputStreamSplitter = new InputStreamSplitter(responseData, outputStreams);
            inputStreamSplitter.copy();

        } catch (IOException e) {
            ioExceptions.add(e);
        }
    }

}
