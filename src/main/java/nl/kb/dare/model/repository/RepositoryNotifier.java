package nl.kb.dare.model.repository;

import nl.kb.dare.endpoints.websocket.StatusSocketRegistrations;

public class RepositoryNotifier {

    public void notifyUpdate() {
        StatusSocketRegistrations.getInstance().broadcast("repository-change");
    }
}
