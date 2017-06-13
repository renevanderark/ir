package nl.kb.dare.endpoints;

import com.google.common.collect.Lists;
import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.model.repository.HarvestSchedule;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.http.HttpResponseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoriesEndpointTest {

    private KbAuthFilter filter = mock(KbAuthFilter.class);

    @Before
    public void setUp() {
        when(filter.getFilterResponse(any())).thenReturn(Optional.empty());
    }


    @Test
    public void createShouldCreateANewRpository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryController repositoryController = mock(RepositoryController.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(filter, dao,  mock(RepositoryValidator.class), repositoryController);
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        final Integer id = 123;
        when(dao.insert(repositoryConfig)).thenReturn(id);

        final Response response = instance.create(repositoryConfig);

        verify(dao).insert(repositoryConfig);
        verify(repositoryController).notifyUpdate();
        assertThat(response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
        assertThat(response.getHeaderString("Location"), equalTo("/repositories/" + id));
        assertThat(response.getEntity(), is(String.format("{\"id\": %d}", id)));
    }

    @Test
    public void deleteShouldDeleteTheRepositoryAndItsRecords() throws IOException {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryController repositoryController = mock(RepositoryController.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(filter, dao, mock(RepositoryValidator.class), repositoryController);
        final Integer id = 123;

        final Response response = instance.delete(id);

        final InOrder inOrder = inOrder(dao, repositoryController);
        inOrder.verify(dao).remove(id);
        inOrder.verify(repositoryController).notifyUpdate();
        inOrder.verifyNoMoreInteractions();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }


    @Test
    public void updateShouldUpdateTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryController repositoryController = mock(RepositoryController.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(filter, dao, mock(RepositoryValidator.class), repositoryController);
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        final Integer id = 123;

        final Response response = instance.update(id, repositoryConfig);

        verify(dao).update(id, repositoryConfig);
        verify(repositoryController).notifyUpdate();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(repositoryConfig));
    }

    @Test
    public void enableShouldUpdateTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryController repositoryController = mock(RepositoryController.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(filter, dao, mock(RepositoryValidator.class), repositoryController);
        final Integer id = 123;

        final Response response = instance.enable(id);

        verify(dao).enable(id);
        verify(repositoryController).notifyUpdate();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void disableShouldUpdateTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryController repositoryController = mock(RepositoryController.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(filter, dao, mock(RepositoryValidator.class), repositoryController);
        final Integer id = 123;

        final Response response = instance.disable(id);

        verify(dao).disable(id);
        verify(repositoryController).notifyUpdate();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }


    @Test
    public void setScheduleShouldUpdateTheRepository() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryController repositoryController = mock(RepositoryController.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(filter, dao, mock(RepositoryValidator.class), repositoryController);
        final Integer id = 123;

        final Response response = instance.setSchedule(id, 2);

        verify(dao).setSchedule(id, 2);
        verify(repositoryController).notifyUpdate();

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void indexShouldRespondWithAListOfRepositories() {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(filter, dao, mock(RepositoryValidator.class), mock(RepositoryController.class));
        final Repository repositoryConfig1 = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY, 1, null);
        final Repository repositoryConfig2 = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY, 2, null);
        final List<Repository> repositories = Lists.newArrayList(repositoryConfig1, repositoryConfig2);

        when(dao.list()).thenReturn(repositories);
        final Response response = instance.index();

        verify(dao).list();
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(repositories));
    }


    @Test
    public void validateNewShouldReturnTheValidationResultForTheRepositoryConfiguration() throws IOException, SAXException, HttpResponseException {
        final RepositoryDao dao = mock(RepositoryDao.class);
        final RepositoryValidator validator = mock(RepositoryValidator.class);
        final RepositoriesEndpoint instance = new RepositoriesEndpoint(filter, dao, validator, mock(RepositoryController.class));
        final Repository repositoryConfig = new Repository("http://example.com", "name", "prefix", "setname", "123", true, HarvestSchedule.DAILY);
        final RepositoryValidator.ValidationResult validationResult = validator.new ValidationResult();
        when(validator.validate(repositoryConfig)).thenReturn(validationResult);

        final Response response = instance.validateNew(repositoryConfig);

        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), equalTo(validationResult));
    }
}