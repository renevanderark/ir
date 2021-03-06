package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReporter;
import nl.kb.dare.model.reporting.ExcelReportBuilder;
import nl.kb.dare.model.reporting.ExcelReportDao;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.dare.model.statuscodes.ProcessStatus;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Path("/record-status")
public class RecordStatusEndpoint {
    private final KbAuthFilter filter;
    private final RecordReporter recordReporter;
    private final ErrorReporter errorReporter;
    private final ExcelReportDao excelReportDao;
    private final ExcelReportBuilder excelReportBuilder;

    public RecordStatusEndpoint(KbAuthFilter filter, RecordReporter recordReporter, ErrorReporter errorReporter,
                                ExcelReportDao excelReportDao, ExcelReportBuilder excelReportBuilder) {
        this.filter = filter;
        this.recordReporter = recordReporter;
        this.errorReporter = errorReporter;
        this.excelReportDao = excelReportDao;
        this.excelReportBuilder = excelReportBuilder;
    }

    @GET
    @Produces("application/json")
    public Response getStatus(@HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() ->
                Response.ok(recordReporter.getStatusUpdate().getData()).build());
    }

    @GET
    @Produces("application/json")
    @Path("/errors")
    public Response getErrorStatus(@HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() ->
            Response.ok(errorReporter.getStatusUpdate().getData()).build());
    }

    @GET
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Path("/errors/{repositoryId}/{sheetName}.xlsx")
    public Response getErrorReport(@PathParam("repositoryId") Integer repositoryId,
                                   @PathParam("sheetName") String sheetName) {
        final StreamingOutput output = out ->
                excelReportBuilder.build(sheetName, excelReportDao.getExcelForRepository(repositoryId), out);

        return Response.ok(output).build();
    }

    @GET
    @Produces("application/json")
    @Path("/status-codes")
    public Response getStatusCodes(@HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() -> {
            final Map<String, Map<Integer, String>> statusCodes = new HashMap<>();

            statusCodes.put("errorStatuses",
                    Stream.of(ErrorStatus.values()).collect(toMap(ErrorStatus::getCode, ErrorStatus::getStatus)));

            statusCodes.put("processStatuses",
                    Stream.of(ProcessStatus.values()).collect(toMap(ProcessStatus::getCode, ProcessStatus::getStatus)));

            return Response
                    .ok(statusCodes)
                    .build();
        });
    }
}
