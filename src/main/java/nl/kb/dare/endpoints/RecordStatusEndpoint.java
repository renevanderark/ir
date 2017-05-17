package nl.kb.dare.endpoints;

import nl.kb.dare.model.preproces.RecordReporter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/record-status")
public class RecordStatusEndpoint {
    private final RecordReporter recordReporter;

    public RecordStatusEndpoint(RecordReporter recordReporter) {
        this.recordReporter = recordReporter;
    }

    @GET
    @Produces("application/json")
    public Response getStatus() {

        return Response.ok(recordReporter.getStatusUpdate()).build();
    }
}
