package nl.kb.dare.endpoints;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class RootEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(RootEndpoint.class);

    private static final String HTML_TEMPLATE;

    static {
        final InputStream resource =
                RootEndpoint.class.getClassLoader().getResourceAsStream("assets/html/root-template.html");
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(resource))) {
            HTML_TEMPLATE = buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final String hostName;
    private final String kbAutLocation;

    public RootEndpoint(String kbAutLocation, String hostName) {
        this.kbAutLocation = kbAutLocation;
        this.hostName = hostName;
    }

    @GET
    public Response getHtml() {
        return Response.ok(parseHtmlTemplate()).build();
    }

    @GET
    @Path("{param1}")
    public Response getHtml(@PathParam("param1") String param1) {
        return Response.ok(parseHtmlTemplate(param1)).build();
    }

    @GET
    @Path("{param1}/{param2}")
    public Response getHtml(@PathParam("param1") String param1, @PathParam("param2") String param2) {
        return Response.ok(parseHtmlTemplate(param1, param2)).build();
    }

    @GET
    @Path("{param1}/{param2}/{param3}")
    public Response getHtml(@PathParam("param1") String param1, @PathParam("param2") String param2,
                            @PathParam("param3") String param3) {
        return Response.ok(parseHtmlTemplate(param1, param2, param3)).build();
    }

    @GET
    @Path("{param1}/{param2}/{param3}/{param4}")
    public Response getHtml(@PathParam("param1") String param1, @PathParam("param2") String param2,
                            @PathParam("param3") String param3, @PathParam("param4") String param4) {
        return Response.ok(parseHtmlTemplate(param1, param2, param3, param4)).build();
    }

    private String parseHtmlTemplate(String... pathParams) {
        final JsEnv env = new JsEnv(pathParams, hostName, kbAutLocation);

        try {
            final String jsEnv = new ObjectMapper().writeValueAsString(env);
            return HTML_TEMPLATE
                    .replace("<%= JS_ENVIRONMENT %>", String.format("var globals = %s;", jsEnv));
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize js env as json", e);
            return HTML_TEMPLATE
                    .replace("<%= JS_ENVIRONMENT %>", String.format("var globals = %s;", "{}"));
        }

    }

    private class JsEnv {
        @JsonProperty
        private final String[] pathParams;
        @JsonProperty
        private final String hostName;
        @JsonProperty
        private final String kbAutLocation;

        JsEnv(String[] pathParams, String hostName, String kbAutLocation) {

            this.pathParams = pathParams;
            this.hostName = hostName;
            this.kbAutLocation = kbAutLocation;
        }
    }
}