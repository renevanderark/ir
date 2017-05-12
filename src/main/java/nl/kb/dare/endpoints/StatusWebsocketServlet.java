package nl.kb.dare.endpoints;

import nl.kb.dare.endpoints.websocket.StatusSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class StatusWebsocketServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(StatusSocket.class);
    }

}
