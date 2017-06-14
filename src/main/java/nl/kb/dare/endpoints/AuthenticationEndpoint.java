package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.kbaut.KbAuthFilter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Path("/authenticate")
public class AuthenticationEndpoint {

    private final KbAuthFilter filter;
    private final String kbAutLocation;
    private final String hostName;

    public AuthenticationEndpoint(KbAuthFilter filter, String kbAutLocation, String hostName) {

        this.filter = filter;
        this.kbAutLocation = kbAutLocation;
        this.hostName = hostName;
    }


    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
    public Response authenticate(@QueryParam("xml") String base64Xml) {
        return filter.getToken(base64Xml).map(token ->
                Response.status(Response.Status.FOUND).header("Location", "/?token=" + token).build()
        ).orElseGet(() ->
                Response.status(Response.Status.FORBIDDEN)
                        .entity("<html><body><h1>Sessie verlopen of login niet geslaagd</h1>" +
                                "<a href='" + getKbAuthLink() + "'>Opnieuw inloggen</a>" +
                                "</body></html>")
                        .build()
        );
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/me")
    public Response getCredentials(@HeaderParam("Authorization") String auth) {
        return filter.getCredentialResponse(auth);
    }

    private String getKbAuthLink() {
        try {
            return  String.format("%s?id=dare2&application=ir-objectharvester&return_url=%s",
                    kbAutLocation,
                    URLEncoder.encode("http://" + hostName + "/authenticate", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return "#";
        }
    }
}
