package nl.kb.dare.model.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.SocketUpdate;

import java.util.List;

class RepositoryUpdate implements SocketUpdate {
    @JsonProperty
    final List<Repository> data;

    RepositoryUpdate(List<Repository> list) {
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
