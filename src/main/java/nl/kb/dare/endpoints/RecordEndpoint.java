package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.StoredErrorReport;
import nl.kb.filestorage.FileStorage;
import nl.kb.filestorage.FileStorageHandle;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.HashMap;

@Path("/records")
public class RecordEndpoint {
    private final KbAuthFilter filter;
    private final RecordDao recordDao;
    private final ErrorReportDao errorReportDao;
    private final FileStorage fileStorage;

    public RecordEndpoint(KbAuthFilter filter, RecordDao recordDao,
                          ErrorReportDao errorReportDao, FileStorage fileStorage) {
        this.filter = filter;
        this.recordDao = recordDao;
        this.errorReportDao = errorReportDao;
        this.fileStorage = fileStorage;
    }

    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@QueryParam("q") String query, @HeaderParam("Authorization") String auth) {
        return filter.getFilterResponse(auth)
                .orElseGet(() -> Response.ok(recordDao.query("%" + query + "%"))
                .build());
    }

    @GET
    @Path("/status/{kbObjId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status(@PathParam("kbObjId") String kbObjId, @HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() -> {
            final Record record = recordDao.findByKbObjId(kbObjId);

            if (record == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            final StoredErrorReport errorReport = errorReportDao.fetchForRecordId(record.getId());
            final HashMap<String, Object> result = new HashMap<>();
            result.put("record", record);
            result.put("errorReport", errorReport);
            return Response.ok(result).build();
        });
    }

    @GET
    @Path("/download/{kbObjId}")
    @Produces("application/zip")
    public Response download(@PathParam("kbObjId") String kbObjId, @HeaderParam("Authorization") String auth) {
        final Record record = recordDao.findByKbObjId(kbObjId);
        if (record == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            final FileStorageHandle fileStorageHandle = fileStorage.create(record.getKbObjId());
            final StreamingOutput downloadOutput = fileStorageHandle::downloadZip;
            return Response.ok(downloadOutput)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + kbObjId + ".zip\"")
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
