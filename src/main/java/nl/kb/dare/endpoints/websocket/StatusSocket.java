package nl.kb.dare.endpoints.websocket;

import nl.kb.dare.websocket.StatusSocketRegistrations;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class StatusSocket {

    private Session session;

    @OnWebSocketConnect
    public void onConnect(Session session){
        this.session = session;
        StatusSocketRegistrations.getInstance().add(this);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        StatusSocketRegistrations.getInstance().remove(this);
    }

    public Session getSession() {
        return session;
    }
}
