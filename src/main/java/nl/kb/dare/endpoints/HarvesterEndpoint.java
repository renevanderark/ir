package nl.kb.dare.endpoints;

import nl.kb.dare.jobs.ScheduledHarvestRunner;
import nl.kb.dare.model.RunState;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/harvesters/{repositoryId}")
public class HarvesterEndpoint {
    private final RepositoryDao repositoryDao;
    private final ScheduledHarvestRunner harvestRunner;


    public HarvesterEndpoint(
            RepositoryDao repositoryDao,
            ScheduledHarvestRunner harvestRunner) {

        this.repositoryDao = repositoryDao;
        this.harvestRunner = harvestRunner;
    }

    @Path("/start")
    @POST
    @Produces("application/json")
    public Response startHarvester(@PathParam("repositoryId") Integer repositoryId) {
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
    }

    @Path("/interrupt")
    @POST
    @Produces("application/json")
    public Response interruptHarvester(@PathParam("repositoryId") Integer repositoryId) {

        final Repository repository = repositoryDao.findById(repositoryId);

        if (repository == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("repository not found",
                            Response.Status.NOT_FOUND.getStatusCode()))
                    .build();
        }

        if (harvestRunner.getHarvesterRunstate(repositoryId) == RunState.RUNNING) {
            harvestRunner.interruptHarvest(repositoryId);
        }

        return Response.ok("{}").build();
    }
}
