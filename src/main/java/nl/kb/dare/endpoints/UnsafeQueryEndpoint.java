package nl.kb.dare.endpoints;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/unsafe")
public class UnsafeQueryEndpoint {
    private final DBI db;

    public UnsafeQueryEndpoint(DBI db) {
        this.db = db;
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(@QueryParam("q") String query) {
        final Handle h = db.open();
        final List<Map<String, Object>> result = h.select(query);

        h.close();
        return Response.ok(result).build();
    }

}
