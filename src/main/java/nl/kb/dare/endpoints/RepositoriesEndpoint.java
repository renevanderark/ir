package nl.kb.dare.endpoints;

import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.http.HttpResponseException;
import org.xml.sax.SAXException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Path("/repositories")
public class RepositoriesEndpoint {
    private RepositoryDao dao;
    private RepositoryValidator validator;
    private final RepositoryController repositoryController;

    public RepositoriesEndpoint(RepositoryDao dao,  RepositoryValidator validator, RepositoryController repositoryController) {
        this.dao = dao;
        this.validator = validator;
        this.repositoryController = repositoryController;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index() {
        final List<Repository> list = dao.list();
        return Response.ok().entity(list).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Repository repositoryConfig) {
        final Integer id = dao.insert(repositoryConfig);
        repositoryController.notifyUpdate();
        return Response.created(URI.create("/repositories/" + id))
                .entity(String.format("{\"id\": %d}", id))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Integer id, Repository repositoryConfig) {
        dao.update(id, repositoryConfig);
        repositoryController.notifyUpdate();
        return Response.ok(repositoryConfig).build();
    }

    @PUT
    @Path("/{id}/enable")
    public Response enable(@PathParam("id") Integer id) {
        dao.enable(id);
        repositoryController.notifyUpdate();
        return Response.ok("{}").build();
    }


    @PUT
    @Path("/{id}/disable")
    public Response disable(@PathParam("id") Integer id) {
        dao.disable(id);
        repositoryController.notifyUpdate();
        return Response.ok("{}").build();
    }

    @PUT
    @Path("/{id}/setSchedule/{scheduleEnumValue}")
    public Response setSchedule(@PathParam("id") Integer id, @PathParam("scheduleEnumValue") Integer scheduleEnumValue) {
        dao.setSchedule(id, scheduleEnumValue);
        repositoryController.notifyUpdate();
        return Response.ok("{}").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/validate")
    public Response validateNew(Repository repositoryConfig) {
        return getValidationResponse(repositoryConfig);
    }

    private Response getValidationResponse(Repository repositoryConfig) {
        try {
            final RepositoryValidator.ValidationResult result = validator.validate(repositoryConfig);
            return Response.ok(result).build();
        } catch (IOException | HttpResponseException e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("repository url could not be reached: " + repositoryConfig.getUrl(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
                    .build();
        } catch (SAXException e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("failed to parse xml response for repository url: " + repositoryConfig.getUrl(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        dao.remove(id);
        repositoryController.notifyUpdate();
        return Response.ok("{}").build();
    }

}
