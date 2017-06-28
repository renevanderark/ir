package nl.kb.dare.websocket;

import com.google.common.collect.Sets;
import nl.kb.dare.endpoints.websocket.StatusSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class StatusSocketRegistrations {
    private static final Logger LOG = LoggerFactory.getLogger(StatusSocketRegistrations.class);

    private static StatusSocketRegistrations instance;
    private Set<StatusSocket> registrations = Sets.newConcurrentHashSet();

    public static StatusSocketRegistrations getInstance() {
        if (instance == null) {
            instance = new StatusSocketRegistrations();
        }
        return instance;
    }

    public void add(StatusSocket socket) {
        registrations.add(socket);
    }

    public void remove(StatusSocket socket) {
        registrations.remove(socket);
    }

    private Set<StatusSocket> get() {
        return registrations;
    }

    public void broadcast(String msg) {
        StatusSocketRegistrations.getInstance().get().forEach(registration -> {
            try {
                registration.getSession().getRemote().sendString(msg);
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to send websocket message", e);
                }
            }
        });
    }

    public boolean hasMembers() {
        return !registrations.isEmpty();
    }
}
