package nl.kb.dare.endpoints;

import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReporter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/record-status")
public class RecordStatusEndpoint {
    private final RecordReporter recordReporter;
    private final ErrorReporter errorReporter;

    public RecordStatusEndpoint(RecordReporter recordReporter, ErrorReporter errorReporter) {
        this.recordReporter = recordReporter;
        this.errorReporter = errorReporter;
    }

    @GET
    @Produces("application/json")
    public Response getStatus() {

        return Response.ok(recordReporter.getStatusUpdate().getData()).build();
    }

    @GET
    @Produces("application/json")
    @Path("/errors")
    public Response getErrorStatus() {

        return Response.ok(errorReporter.getStatusUpdate().getData()).build();
    }
}
