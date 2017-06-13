package nl.kb.dare.endpoints.kbaut;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

public class KbAuthFilter {
    private final boolean enabled;
    private final byte[] salt;

    public KbAuthFilter(boolean enabled) {
        final Random rand = new Random((new Date()).getTime());
        this.enabled = enabled;
        this.salt = new byte[8];;
        rand.nextBytes(salt);
    }

    public Optional<Response> getFilterResponse(String authHeader) {
        if (!enabled || isValid(authHeader)) {
            return Optional.empty();
        }

        return Optional.of(Response.status(Response.Status.FORBIDDEN).build());
    }

    private boolean isValid(String authHeader) {

        return authHeader != null;
    }

    public Optional<String> getToken(String base64Xml) {
        if (isValid(base64Xml)) {
            return Optional.of(base64Xml);
        }

        return Optional.empty();
    }
}
