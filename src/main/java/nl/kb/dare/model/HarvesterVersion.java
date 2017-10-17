package nl.kb.dare.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HarvesterVersion {
    @JsonProperty
    private String name;
    @JsonProperty
    private String version;

    public HarvesterVersion() {

    }

    public HarvesterVersion(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
