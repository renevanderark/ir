package nl.kb.dare.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.kb.dare.endpoints.websocket.StatusSocketRegistrations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(SocketNotifier.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void notifyUpdate(SocketUpdate socketUpdate) {
        final String msg;
        try {
            msg = objectMapper.writeValueAsString(socketUpdate);
            StatusSocketRegistrations.getInstance().broadcast(msg);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to produce json from RecordBatchLoader ", e);
        }
    }

}
