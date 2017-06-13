package nl.kb.dare.endpoints.kbaut;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import sun.misc.BASE64Decoder;

import javax.ws.rs.core.Response;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.Optional;

public class KbAuthFilter {
    private final boolean enabled;
    private final BASE64Decoder decoder = new BASE64Decoder();
    private static final SAXParser saxParser;

    static {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sax parser", e);
        }
    }


    public KbAuthFilter(boolean enabled) {
        this.enabled = enabled;
    }

    public Optional<Response> getFilterResponse(String authHeader) {
        if (!enabled || isValid(authHeader)) {
            return Optional.empty();
        }

        return Optional.of(Response.status(Response.Status.FORBIDDEN).build());
    }

    private boolean isValid(String authHeader) {
        if (authHeader == null) {
            return false;
        }
        try {
            final String kbAutXml = new String(decoder.decodeBuffer(authHeader));
            final KbAutXmlHandler kbAutXmlHandler = new KbAutXmlHandler();
            synchronized (saxParser) {
                saxParser.parse(IOUtils.toInputStream(kbAutXml, "UTF-8"), kbAutXmlHandler);
            }

            return kbAutXmlHandler.isValid();
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<String> getToken(String base64Xml) {
        if (isValid(base64Xml)) {
            return Optional.of(base64Xml);
        }

        return Optional.empty();
    }
}
