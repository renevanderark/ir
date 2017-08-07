package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.StoredErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.filestorage.FileStorage;
import nl.kb.filestorage.FileStorageHandle;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
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
    private final RepositoryDao repositoryDao;
    private final FileStorage fileStorage;
    private final RecordReporter recordReporter;
    private final SocketNotifier socketNotifier;

    public RecordEndpoint(KbAuthFilter filter, RecordDao recordDao,
                          ErrorReportDao errorReportDao, RepositoryDao repositoryDao, FileStorage fileStorage,
                          RecordReporter recordReporter, SocketNotifier socketNotifier) {
        this.filter = filter;
        this.recordDao = recordDao;
        this.errorReportDao = errorReportDao;
        this.repositoryDao = repositoryDao;
        this.fileStorage = fileStorage;
        this.recordReporter = recordReporter;
        this.socketNotifier = socketNotifier;
    }

    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@QueryParam("q") String query, @HeaderParam("Authorization") String auth) {
        return filter.getFilterResponse(auth)
                .orElseGet(() -> Response.ok(recordDao.query("%" + query + "%"))
                .build());
    }



    @PUT
    @Path("/bulk-reset/{repositoryId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response bulkReset(@PathParam("repositoryId") Integer repositoryId,
                              @HeaderParam("Authorization") String auth) {
        return filter.getFilterResponse(auth)
                .orElseGet(() -> {
                    errorReportDao.bulkDeleteForRepository(ProcessStatus.FAILED.getCode(), repositoryId);
                    recordDao.bulkUpdateState(ProcessStatus.FAILED.getCode(), ProcessStatus.PENDING.getCode(),
                            repositoryId);

                    socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());

                    return Response.ok("{}").build();
                });
    }

    @PUT
    @Path("/reset/{kbObjId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(@PathParam("kbObjId") String kbObjId, @HeaderParam("Authorization") String auth) {
        return filter.getFilterResponse(auth).orElseGet(() -> {
            final Record record = recordDao.findByKbObjId(kbObjId);
            if (record == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            record.setState(ProcessStatus.PENDING);
            recordDao.updateState(record);
            errorReportDao.deleteForRecordId(record.getId());
            socketNotifier.notifyUpdate(recordReporter.getStatusUpdate());

            return Response.ok("{}").build();
        });
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
        final Repository repository = repositoryDao.findById(record.getRepositoryId());
        if (record == null || repository == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            final String superSet = repository.getSet().replaceAll(":.*$", "");
            final FileStorageHandle fileStorageHandle = fileStorage.create(String.format("%s/%s_%s",
                    superSet, superSet, record.getKbObjId()));
            final StreamingOutput downloadOutput = fileStorageHandle::downloadZip;
            return Response.ok(downloadOutput)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + kbObjId + ".zip\"")
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
