package nl.kb.dare.integrationtest.oai.endpoints;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;


@Path("/")
public class OaiEndpoint {

    @Path("/oai")
    @GET
    @Produces("text/xml")
    public Response get(
            @QueryParam("verb") String verb,
            @QueryParam("resumptionToken") String resumptionToken,
            @QueryParam("from") String from,
            @QueryParam("identifier") String identifier
    ) {

        switch (verb) {
            case "ListIdentifiers":
                return listIdentifiers(resumptionToken, from);
            default:
        }
        return Response.ok().build();
    }

    private Response listIdentifiers(String resumptionToken, String from) {

        final InputStream in = resumptionToken == null
                ? OaiEndpoint.class.getResourceAsStream("/integrationtest/oairesponses/ListIdentifiersWithResumptionToken.xml")
                :  OaiEndpoint.class.getResourceAsStream("/integrationtest/oairesponses/ListIdentifiersWithoutResumptionToken.xml");
        final StreamingOutput responseData = output -> IOUtils.copy(in, output);
        return Response.ok(responseData).build();
    }

    @Path("/oai-infinite")
    @GET
    @Produces("text/xml")
    public Response getInfinite(
            @QueryParam("verb") String verb,
            @QueryParam("resumptionToken") String resumptionToken,
            @QueryParam("from") String from,
            @QueryParam("identifier") String identifier
    ) {

        final InputStream in = OaiEndpoint.class.getResourceAsStream("/integrationtest/oairesponses/ListIdentifiersWithResumptionToken.xml");
        final StreamingOutput responseData = output -> IOUtils.copy(in, output);
        return Response.ok(responseData).build();
    }

    @Path("/oai-fail")
    @GET
    @Produces("text/xml")
    public Response getFail(
            @QueryParam("verb") String verb,
            @QueryParam("resumptionToken") String resumptionToken,
            @QueryParam("from") String from,
            @QueryParam("identifier") String identifier
    ) {

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

}
