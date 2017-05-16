package nl.kb.dare.endpoints;

import nl.kb.dare.jobs.RepositoryHarvester;
import nl.kb.dare.model.RunState;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.oaipmh.ListIdentifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/harvesters/{repositoryId}")
public class HarvesterEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(HarvesterEndpoint.class);

    private final RepositoryDao repositoryDao;
    private final RepositoryController repositoryController;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    public HarvesterEndpoint(RepositoryDao repositoryDao, RepositoryController repositoryController,
                             HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {

        this.repositoryDao = repositoryDao;
        this.repositoryController = repositoryController;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
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

        if (repository.getRunState() != RunState.WAITING) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("harvest already running",
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

        final RepositoryHarvester repositoryHarvester = RepositoryHarvester
                .getInstance(repository, repositoryController, httpFetcher, responseHandlerFactory);

        new Thread(repositoryHarvester).start();

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

        final Optional<ListIdentifiers> runningInstance = RepositoryHarvester
                .getRunningInstance(repositoryId);

        if (runningInstance.isPresent()) {
            runningInstance.get().interruptHarvest();
            repositoryController.onHarvestInterrupt(repositoryId);
        } else {
            repositoryDao.setRunState(repositoryId, RunState.WAITING.getCode());
        }

        return Response.ok("{}").build();
    }
}
