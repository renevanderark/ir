package nl.kb.dare.model.repository;

import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.dare.websocket.socketupdate.RepositoryUpdate;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class RepositoryControllerTest {

    public static final String THE_DATESTAMP = "the-datestamp";

    @Test
    public void notifyUpdateShouldInvokeSocketNotifierWithCurrentStateOfRepositories() {
        final SocketNotifier socketNotifier = mock(SocketNotifier.class);
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final RepositoryController instance = new RepositoryController(repositoryDao, socketNotifier);
        final List<Repository> returnedList = new ArrayList<>();
        when(repositoryDao.list()).thenReturn(returnedList);

        instance.notifyUpdate();

        final InOrder inOrder = inOrder(repositoryDao, socketNotifier);
        inOrder.verify(repositoryDao).list();
        inOrder.verify(socketNotifier).notifyUpdate(argThat(allOf(
                is(instanceOf(RepositoryUpdate.class)),
                hasProperty("data", is(returnedList))
        )));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void onHarvestCompleteShouldSetTheDatestampAndNotifyUpdate() {
        final SocketNotifier socketNotifier = mock(SocketNotifier.class);
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final RepositoryController instance = new RepositoryController(repositoryDao, socketNotifier);

        instance.onHarvestComplete(2, THE_DATESTAMP);

        final InOrder inOrder = inOrder(repositoryDao, socketNotifier);
        inOrder.verify(repositoryDao).setDateStamp(2, THE_DATESTAMP);
        inOrder.verify(socketNotifier).notifyUpdate(any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void onHarvestExceptionShouldDisableAllRepositoriesAndNotifyUpdate() {
        final SocketNotifier socketNotifier = mock(SocketNotifier.class);
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final RepositoryController instance = new RepositoryController(repositoryDao, socketNotifier);

        instance.onHarvestException(2, mock(Exception.class));

        final InOrder inOrder = inOrder(repositoryDao, socketNotifier);
        inOrder.verify(repositoryDao).disableAll();
        inOrder.verify(socketNotifier).notifyUpdate(any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void onHarvestProgressShouldSetTheDatestampAndNotifyUpdate() {
        final SocketNotifier socketNotifier = mock(SocketNotifier.class);
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final RepositoryController instance = new RepositoryController(repositoryDao, socketNotifier);

        instance.onHarvestProgress(2, THE_DATESTAMP);

        final InOrder inOrder = inOrder(repositoryDao, socketNotifier);
        inOrder.verify(repositoryDao).setDateStamp(2, THE_DATESTAMP);
        inOrder.verify(socketNotifier).notifyUpdate(any());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void beforeHarvestShouldStoreANewLastHarvestDate() {
        final SocketNotifier socketNotifier = mock(SocketNotifier.class);
        final RepositoryDao repositoryDao = mock(RepositoryDao.class);
        final RepositoryController instance = new RepositoryController(repositoryDao, socketNotifier);

        instance.beforeHarvest(2);

        final InOrder inOrder = inOrder(repositoryDao, socketNotifier);
        inOrder.verify(repositoryDao).setLastHarvest(2);
        inOrder.verify(socketNotifier).notifyUpdate(any());
        inOrder.verifyNoMoreInteractions();
    }
}