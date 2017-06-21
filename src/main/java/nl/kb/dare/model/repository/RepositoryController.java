package nl.kb.dare.model.repository;

import nl.kb.dare.model.SocketNotifier;

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

    public void onHarvestComplete(Integer id, String dateStamp) {
        synchronized (repositoryDao) {
            repositoryDao.setDateStamp(id, dateStamp);
        }
        notifyUpdate();
    }

    public void onHarvestException(Integer id, Exception exception) {
        synchronized (repositoryDao) {
            repositoryDao.disableAll();
        }
        notifyUpdate();
    }

    public void onHarvestProgress(Integer id, String dateStamp) {
        synchronized (repositoryDao) {
            repositoryDao.setDateStamp(id, dateStamp);
        }
        notifyUpdate();
    }

    public void beforeHarvest(Integer id) {
        synchronized (repositoryDao) {
            repositoryDao.setLastHarvest(id);
        }
        notifyUpdate();
    }

}
