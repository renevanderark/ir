package nl.kb.dare.endpoints;

import com.google.common.collect.Lists;
import nl.kb.dare.model.repository.HarvestSchedule;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryNotifier;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.http.HttpResponseException;
import org.junit.Test;
import org.mockito.InOrder;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoriesEndpointTest {

    @Test
    public void createShouldCreateANewRpository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryNotifier repositoryNotifier = mock(RepositoryNotifier.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao,  mock(RepositoryValidator.class), repositoryNotifier);
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        final Integer id = 123;
        when(dao.insert(repositoryConfig)).thenReturn(id);

        final Response response = instance.create(repositoryConfig);

        verify(dao).insert(repositoryConfig);
        verify(repositoryNotifier).notifyUpdate();
        assertThat(response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
        assertThat(response.getHeaderString("Location"), equalTo("/repositories/" + id));
        assertThat(response.getEntity(), is(String.format("{\"id\": %d}", id)));
    }

    @Test
    public void deleteShouldDeleteTheRepositoryAndItsRecords() throws IOException {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryNotifier repositoryNotifier = mock(RepositoryNotifier.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, mock(RepositoryValidator.class), repositoryNotifier);
        final Integer id = 123;

        final Response response = instance.delete(id);

        final InOrder inOrder = inOrder(dao, repositoryNotifier);
        inOrder.verify(dao).remove(id);
        inOrder.verify(repositoryNotifier).notifyUpdate();
        inOrder.verifyNoMoreInteractions();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void getShouldReturnTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, mock(RepositoryValidator.class), mock(RepositoryNotifier.class));
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        final Integer id = 123;
        when(dao.findById(id)).thenReturn(repositoryConfig);

        final Response response = instance.get(id);

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(repositoryConfig));
    }

    @Test
    public void getShouldReturnNotFoundWhenRepositoryIsNotFound() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, mock(RepositoryValidator.class), mock(RepositoryNotifier.class));
        final Integer id = 123;
        when(dao.findById(id)).thenReturn(null);

        final Response response = instance.get(id);

        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        assertThat(response.getEntity(), hasProperty("message", equalTo("repository not found with id: 123")));
        assertThat(response.getEntity(), hasProperty("code", equalTo(Response.Status.NOT_FOUND.getStatusCode())));
    }

    @Test
    public void updateShouldUpdateTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryNotifier repositoryNotifier = mock(RepositoryNotifier.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, mock(RepositoryValidator.class), repositoryNotifier);
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        final Integer id = 123;

        final Response response = instance.update(id, repositoryConfig);

        verify(dao).update(id, repositoryConfig);
        verify(repositoryNotifier).notifyUpdate();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(repositoryConfig));
    }

    @Test
    public void enableShouldUpdateTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryNotifier repositoryNotifier = mock(RepositoryNotifier.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, mock(RepositoryValidator.class), repositoryNotifier);
        final Integer id = 123;

        final Response response = instance.enable(id);

        verify(dao).enable(id);
        verify(repositoryNotifier).notifyUpdate();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void disableShouldUpdateTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryNotifier repositoryNotifier = mock(RepositoryNotifier.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, mock(RepositoryValidator.class), repositoryNotifier);
        final Integer id = 123;

        final Response response = instance.disable(id);

        verify(dao).disable(id);
        verify(repositoryNotifier).notifyUpdate();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }


    @Test
    public void setScheduleShouldUpdateTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryNotifier repositoryNotifier = mock(RepositoryNotifier.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, mock(RepositoryValidator.class), repositoryNotifier);
        final Integer id = 123;

        final Response response = instance.setSchedule(id, 2);

        verify(dao).setSchedule(id, 2);
        verify(repositoryNotifier).notifyUpdate();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void indexShouldRespondWithAListOfRepositories() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, mock(RepositoryValidator.class), mock(RepositoryNotifier.class));
        final Repository repositoryConfig1 = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY, 1);
        final Repository repositoryConfig2 = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY, 2);
        final List<Repository> repositories = Lists.newArrayList(repositoryConfig1, repositoryConfig2);

        when(dao.list()).thenReturn(repositories);
        final Response response = instance.index();

        verify(dao).list();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(repositories));
    }

    @Test
    public void validateShouldReturnTheValidationResultForTheRepositoryConfiguration() throws IOException, SAXException, HttpResponseException {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryValidator validator = mock(RepositoryValidator.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, validator, mock(RepositoryNotifier.class));
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        final Integer id = 123;
        final RepositoryValidator.ValidationResult validationResult = validator.new ValidationResult();
        when(dao.findById(id)).thenReturn(repositoryConfig);
        when(validator.validate(repositoryConfig)).thenReturn(validationResult);

        final Response response = instance.validate(id);

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(validationResult));
    }
    @Test
    public void validateNewShouldReturnTheValidationResultForTheRepositoryConfiguration() throws IOException, SAXException, HttpResponseException {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryValidator validator = mock(RepositoryValidator.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, validator, mock(RepositoryNotifier.class));
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        final RepositoryValidator.ValidationResult validationResult = validator.new ValidationResult();
        when(validator.validate(repositoryConfig)).thenReturn(validationResult);

        final Response response = instance.validateNew(repositoryConfig);

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(validationResult));
    }

    @Test
    public void validateShouldReturnNotFoundWhenRepositoryIsNotFound() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, mock(RepositoryValidator.class), mock(RepositoryNotifier.class));
        final Integer id = 123;
        when(dao.findById(id)).thenReturn(null);

        final Response response = instance.validate(id);

        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        assertThat(response.getEntity(), hasProperty("message", equalTo("repository not found with id: 123")));
        assertThat(response.getEntity(), hasProperty("code", equalTo(Response.Status.NOT_FOUND.getStatusCode())));
    }


    @Test
    public void validateShouldHandleSAXException() throws IOException, SAXException, HttpResponseException {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryValidator validator = mock(RepositoryValidator.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, validator, mock(RepositoryNotifier.class));
        final Integer id = 123;
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        when(dao.findById(id)).thenReturn(repositoryConfig);
        when(validator.validate(repositoryConfig)).thenThrow(SAXException.class);

        final Response response = instance.validate(id);

        assertThat(response.getStatus(), equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        assertThat(response.getEntity(), hasProperty("message", equalTo("failed to parse xml response for repository url: http://example.com")));
        assertThat(response.getEntity(), hasProperty("code", equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())));
    }

    @Test
    public void validateShouldHandleIOException() throws IOException, SAXException, HttpResponseException {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryValidator validator = mock(RepositoryValidator.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(dao, validator, mock(RepositoryNotifier.class));
        final Integer id = 123;
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        when(dao.findById(id)).thenReturn(repositoryConfig);
        when(validator.validate(repositoryConfig)).thenThrow(IOException.class);

        final Response response = instance.validate(id);

        assertThat(response.getStatus(), equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
        assertThat(response.getEntity(), hasProperty("message", equalTo("repository url could not be reached: http://example.com")));
        assertThat(response.getEntity(), hasProperty("code", equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())));
    }
}