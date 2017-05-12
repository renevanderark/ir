package nl.kb.dare.endpoints.websocket;

import com.google.common.collect.Sets;
import org.eclipse.jetty.websocket.api.WebSocketException;

import java.io.IOException;
import java.util.Set;

public class StatusSocketRegistrations {
    private static StatusSocketRegistrations instance;
    private Set<StatusSocket> registrations = Sets.newConcurrentHashSet();

    static public StatusSocketRegistrations getInstance() {
        if (instance == null) {
            instance = new StatusSocketRegistrations();
        }
        return instance;
    }

    void add(StatusSocket socket) {
        registrations.add(socket);
    }

    void remove(StatusSocket socket) {
        registrations.remove(socket);
    }

    Set<StatusSocket> get() {
        return registrations;
    }

    public void broadcast(String msg) {
        StatusSocketRegistrations.getInstance().get().forEach(registration -> {
            try {
                registration.session.getRemote().sendString(msg);
            } catch (IOException | WebSocketException ignored) {
            }
        });
    }

    public boolean hasMembers() {
        return !registrations.isEmpty();
    }
}
