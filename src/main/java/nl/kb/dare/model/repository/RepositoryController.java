package nl.kb.dare.model.repository;

import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.dare.websocket.socketupdate.RepositoryUpdate;

public class RepositoryController {

    private final RepositoryDao repositoryDao;
    private final SocketNotifier socketNotifier;

    public RepositoryController(RepositoryDao repositoryDao, SocketNotifier socketNotifier) {
        this.repositoryDao = repositoryDao;
        this.socketNotifier = socketNotifier;
    }


    public void notifyUpdate() {
        socketNotifier.notifyUpdate(new RepositoryUpdate(repositoryDao.list()));
    }

    public void disableAllRepositories() {
        synchronized (repositoryDao) {
            repositoryDao.disableAll();
        }
        notifyUpdate();
    }

    public void storeHarvestDateStamp(Integer id, String dateStamp) {
        synchronized (repositoryDao) {
            repositoryDao.setDateStamp(id, dateStamp);
        }
        notifyUpdate();
    }

    public void storeHarvestStartTime(Integer id) {
        synchronized (repositoryDao) {
            repositoryDao.setLastHarvest(id);
        }
        notifyUpdate();
    }

}
