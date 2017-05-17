package nl.kb.dare.model.repository;

import nl.kb.dare.model.RunState;
import nl.kb.dare.model.SocketNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryController {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryController.class);

    private final RepositoryDao repositoryDao;
    private final SocketNotifier socketNotifier;

    public RepositoryController(RepositoryDao repositoryDao, SocketNotifier socketNotifier) {
        this.repositoryDao = repositoryDao;
        this.socketNotifier = socketNotifier;
    }


    public void notifyUpdate() {
        socketNotifier.notifyUpdate(new RepositoryUpdate(repositoryDao.list()));
    }

    public void onHarvestComplete(Integer id, String dateStamp) {
        LOG.debug("onHarvestComplete {} {}", id, dateStamp);
        synchronized (repositoryDao) {
            repositoryDao.setDateStamp(id, dateStamp);
            repositoryDao.setRunState(id, RunState.WAITING.getCode());
        }
        notifyUpdate();
    }

    public void onHarvestException(Integer id, Exception exception) {
        /* TODO handle harvester exception? */
    }

    public void onHarvestProgress(Integer id, String dateStamp) {
        synchronized (repositoryDao) {
            repositoryDao.setDateStamp(id, dateStamp);
        }
        notifyUpdate();
    }

    public void beforeHarvest(Integer id) {
        synchronized (repositoryDao) {
            repositoryDao.setRunState(id, RunState.RUNNING.getCode());
        }
        notifyUpdate();
    }

    public void onHarvestInterrupt(Integer id) {
        synchronized (repositoryDao) {
            repositoryDao.setRunState(id, RunState.INTERRUPTED.getCode());
        }
        notifyUpdate();
    }

}
