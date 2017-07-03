package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.scheduledjobs.IdentifierHarvestSchedulerDaemon;
import nl.kb.dare.model.RunState;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/harvesters")
public class HarvesterEndpoint {
    private final KbAuthFilter filter;
    private final RepositoryDao repositoryDao;
    private final IdentifierHarvestSchedulerDaemon harvestRunner;


    public HarvesterEndpoint(
            KbAuthFilter filter, RepositoryDao repositoryDao,
            IdentifierHarvestSchedulerDaemon harvestRunner) {
        this.filter = filter;

        this.repositoryDao = repositoryDao;
        this.harvestRunner = harvestRunner;
    }

    @Path("/{repositoryId}/start")
    @POST
    @Produces("application/json")
    public Response startHarvester(
            @PathParam("repositoryId") Integer repositoryId,
            @HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() -> {

            final Repository repository = repositoryDao.findById(repositoryId);

            if (repository == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("repository not found",
                                Response.Status.NOT_FOUND.getStatusCode()))
                        .build();
            }

            if (harvestRunner.getHarvesterRunstate(repository.getId()) != RunState.WAITING) {
                return Response
                        .status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse("harvest already queued or running",
                                Response.Status.CONFLICT.getStatusCode()))
                        .build();
            }

            if (!repository.getEnabled()) {
                return Response
                        .status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse("repository is disabled",
                                Response.Status.CONFLICT.getStatusCode()))
                        .build();
            }

            harvestRunner.startHarvest(repository.getId());

            return Response.ok("{}").build();
        });
    }

    @Path("/{repositoryId}/interrupt")
    @POST
    @Produces("application/json")
    public Response interruptHarvester(
            @PathParam("repositoryId") Integer repositoryId,
            @HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() -> {
            final Repository repository = repositoryDao.findById(repositoryId);

            if (repository == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("repository not found",
                                Response.Status.NOT_FOUND.getStatusCode()))
                        .build();
            }

            if (harvestRunner.getHarvesterRunstate(repositoryId) == RunState.RUNNING ||
                    harvestRunner.getHarvesterRunstate(repositoryId) == RunState.QUEUED) {
                harvestRunner.interruptHarvest(repositoryId);
            }

            return Response.ok("{}").build();
        });
    }

    @Path("/status")
    @GET
    @Produces("application/json")
    public Response getStatus(
            @HeaderParam("Authorization") String auth) {

        return filter.getFilterResponse(auth).orElseGet(() ->
                Response.ok(harvestRunner.getStatusUpdate().getData()).build());

    }
}
