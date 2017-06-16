package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.filestorage.FileStorage;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/records")
public class RecordEndpoint {
    private final KbAuthFilter filter;
    private final RecordDao recordDao;
    private final FileStorage fileStorage;

    public RecordEndpoint(KbAuthFilter filter, RecordDao recordDao, FileStorage fileStorage) {
        this.filter = filter;
        this.recordDao = recordDao;
        this.fileStorage = fileStorage;
    }

    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@QueryParam("q") String query, @HeaderParam("Authorization") String auth) {
        return filter.getFilterResponse(auth).orElseGet(() -> Response.ok(recordDao.query("%" + query + "%")).build());
    }

}
