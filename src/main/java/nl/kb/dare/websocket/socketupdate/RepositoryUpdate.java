package nl.kb.dare.websocket.socketupdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.websocket.SocketUpdate;

import java.util.List;

public class RepositoryUpdate implements SocketUpdate {
    @JsonProperty
    final List<Repository> data;

    public RepositoryUpdate(List<Repository> list) {
        this.data = list;
    }

    @Override
    public String getType() {
        return "repository-change";
    }

    @Override
    public Object getData() {

        return data;
    }
}
