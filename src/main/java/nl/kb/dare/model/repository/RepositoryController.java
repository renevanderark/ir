package nl.kb.dare.model.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kb.dare.endpoints.websocket.StatusSocketRegistrations;
import nl.kb.dare.model.RunState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RepositoryController {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RepositoryDao repositoryDao;

    private class RepositoryUpdate {
        @JsonProperty
        final String type = "repository-change";
        @JsonProperty
        final List<Repository> data;

        RepositoryUpdate(List<Repository> list) {
            this.data = list;
        }
    }

    public RepositoryController(RepositoryDao repositoryDao) {
        this.repositoryDao = repositoryDao;
    }


    public void notifyUpdate() {

        final String msg;
        try {
            msg = objectMapper.writeValueAsString(new RepositoryUpdate(repositoryDao.list()));
            StatusSocketRegistrations.getInstance().broadcast(msg);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to produce json from RepositoryUpdate ", e);
        }
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

    public void onHarvestProgress(Integer id, String message) {
        LOG.info(message);
        /* TODO detailed notify update only for repo identified by id notifyUpdate();*/
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
