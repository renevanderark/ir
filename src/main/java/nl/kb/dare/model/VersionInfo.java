package nl.kb.dare.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VersionInfo {
    @JsonProperty
    private String harvesterName;
    @JsonProperty
    private String harvesterVersion;
    @JsonProperty
    private String tikaName;
    @JsonProperty
    private String tikaVersion;

    public VersionInfo() {

    }

    public VersionInfo(String harvesterName, String harvesterVersion) {
        this.harvesterName = harvesterName;
        this.harvesterVersion = harvesterVersion;
    }

    public String getHarvesterName() {
        return harvesterName;
    }

    public String getHarvesterVersion() {
        return harvesterVersion;
    }

    public String getTikaName() {
        return tikaName;
    }

    public String getTikaVersion() {
        return tikaVersion;
    }
}
