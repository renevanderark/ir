package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.scheduledjobs.ObjectHarvestSchedulerDaemon;
import nl.kb.dare.websocket.socketupdate.ObjectHarvesterRunstateUpdate;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/workers")
public class ObjectHarvesterEndpoint {
    private final KbAuthFilter filter;
    private final ObjectHarvestSchedulerDaemon objectHarvestSchedulerDaemon;

    public ObjectHarvesterEndpoint(KbAuthFilter filter, ObjectHarvestSchedulerDaemon objectHarvestSchedulerDaemon) {
        this.filter = filter;

        this.objectHarvestSchedulerDaemon = objectHarvestSchedulerDaemon;
    }

    @PUT
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(@HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() -> {
            objectHarvestSchedulerDaemon.enable();
            return Response.ok("{}").build();
        });
    }

    @PUT
    @Path("/disable")
    @Produces(MediaType.APPLICATION_JSON)
    public Response disable(@HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() -> {
            objectHarvestSchedulerDaemon.disable();
            return Response.ok("{}").build();
        });
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus(@HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() ->
                Response.ok((new ObjectHarvesterRunstateUpdate(objectHarvestSchedulerDaemon.getRunState()))).build());
    }
}
