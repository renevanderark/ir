package nl.kb.dare.model.repository;

public class RepositoryNotifier {

    private boolean updated = false;

    public void notifyUpdate() {
        this.updated = true;
    }

    public boolean wasUpdated() {
        boolean notification = updated;
        if (updated) {
            updated = false;
        }
        return notification;
    }
}
