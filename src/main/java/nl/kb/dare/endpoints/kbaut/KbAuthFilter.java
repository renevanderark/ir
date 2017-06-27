package nl.kb.dare.endpoints.kbaut;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import sun.misc.BASE64Decoder;

import javax.ws.rs.core.Response;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class KbAuthFilter {
    private static final Logger LOG = LoggerFactory.getLogger(KbAuthFilter.class);

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



    public Optional<String> getToken(String base64Xml) {
        if (isValid(base64Xml)) {
            return Optional.of(base64Xml);
        }

        return Optional.empty();
    }

    public Response getCredentialResponse(String authHeader) {
        final Optional<KbAutXmlHandler> parsedAuth = parseAuthXml(authHeader);

        if (parsedAuth.isPresent() && parsedAuth.get().isValid()) {
            return Response.ok("{ \"username\": \"" + parsedAuth.get().getUsername() +  "\"}").build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    private boolean isValid(String authHeader) {

        final Optional<KbAutXmlHandler> parsedAuth = parseAuthXml(authHeader);
        return parsedAuth.isPresent() && parsedAuth.get().isValid();
    }

    private Optional<KbAutXmlHandler> parseAuthXml(String authHeader) {

        if (authHeader == null) {
            return Optional.empty();
        }

        try {
            final String kbAutXml = new String(decoder.decodeBuffer(authHeader));
            final KbAutXmlHandler kbAutXmlHandler = new KbAutXmlHandler();
            synchronized (saxParser) {
                saxParser.parse(IOUtils.toInputStream(kbAutXml, StandardCharsets.UTF_8.name()), kbAutXmlHandler);
            }
            return Optional.of(kbAutXmlHandler);
        } catch (IOException | SAXException e) {
            LOG.warn("Failed to decode / parse kbaut xml", e);
            return Optional.empty();
        }
    }
}
