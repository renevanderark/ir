package nl.kb.http.responsehandlers;

import nl.kb.http.HttpResponseHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ResponseHandlerFactory {

    public HttpResponseHandler getSaxParsingHandler(DefaultHandler saxHandler) {
        return new SaxParsingResponseHandler(saxHandler);
    }

    public HttpResponseHandler getStreamCopyingResponseHandler(OutputStream... outputStreams) {
        return new StreamCopyingResponseHandler(outputStreams);
    }

    public ErrorReportingResponseHandler getBaseHandler(Consumer<InputStream> onData) {
        return new ErrorReportingResponseHandler() {
            @Override
            public void onResponseData(Integer status, InputStream responseData, Map<String, List<String>> headerFields) {
                onData.accept(responseData);
            }
        };
    }

}
