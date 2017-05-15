package nl.kb.dare.model.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kb.dare.endpoints.websocket.StatusSocketRegistrations;
import nl.kb.dare.model.RunState;
import nl.kb.oaipmh.OaiRecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RepositoryNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryNotifier.class);
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

    public RepositoryNotifier(RepositoryDao repositoryDao) {
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
        synchronized (repositoryDao) {
            repositoryDao.setRunState(id, RunState.WAITING.getCode());
        }
        notifyUpdate();
    }

    public void onOaiRecord(Integer id, OaiRecordHeader oaiRecordHeader) {
        /* TODO move to record notifier */
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
}
