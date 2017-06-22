package nl.kb.dare.integrationtest.numbers.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


@Path("/numbers")
public class NumbersEndPoint {

    @GET
    @Path("/nbn")
    @Produces("text/xml")
    public Response get(@QueryParam("qt") Integer quantity) {

        return Response.ok("<nbn-set>" + makeNumbers(quantity) + "</nbn-set>").build();
    }

    @GET
    @Path("/crash")
    public Response crash() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    private String makeNumbers(Integer quantity) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < quantity; i++) {
            sb.append(String.format("<nbn>%d</nbn>", i));
        }
        return sb.toString();
    }
}
