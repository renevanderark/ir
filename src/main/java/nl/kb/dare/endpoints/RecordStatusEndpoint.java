package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReporter;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Path("/record-status")
public class RecordStatusEndpoint {
    private final RecordReporter recordReporter;
    private final ErrorReporter errorReporter;

    public RecordStatusEndpoint(KbAuthFilter filter, RecordReporter recordReporter, ErrorReporter errorReporter) {
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

    @GET
    @Produces("application/json")
    @Path("/status-codes")
    public Response getStatusCodes() {
        final Map<String, Map<Integer, String>> statusCodes = new HashMap<>();

        statusCodes.put("errorStatuses",
                Stream.of(ErrorStatus.values()).collect(toMap(ErrorStatus::getCode, ErrorStatus::getStatus)));

        statusCodes.put("processStatuses",
                Stream.of(ProcessStatus.values()).collect(toMap(ProcessStatus::getCode, ProcessStatus::getStatus)));

        return Response
                .ok(statusCodes)
                .build();
    }
}
