package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.jobs.ScheduledOaiRecordFetcher;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/workers")
public class OaiRecordFetcherEndpoint {
    private final ScheduledOaiRecordFetcher oaiRecordFetcher;

    public OaiRecordFetcherEndpoint(KbAuthFilter filter, ScheduledOaiRecordFetcher oaiRecordFetcher) {

        this.oaiRecordFetcher = oaiRecordFetcher;
    }

    @PUT
    @Path("/start")
    @Produces("application/json")
    public Response start() {
        oaiRecordFetcher.enable();
        return Response.ok("{}").build();
    }

    @PUT
    @Path("/disable")
    @Produces("application/json")
    public Response disable() {
        oaiRecordFetcher.disable();
        return Response.ok("{}").build();
    }
}
