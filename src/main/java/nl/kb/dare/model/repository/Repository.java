package nl.kb.dare.model.repository;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

    private Boolean enabled;
    private Integer id;
    private String url;
    private String metadataPrefix;
    private String set;
    private String dateStamp;
    private String name;

    private HarvestSchedule schedule;
    @JsonIgnore
    private LocalDate lastHarvest;

    public Repository() {

    }

    private Repository(RepositoryBuilder repositoryBuilder) {
        this.url = repositoryBuilder.url;
        this.name = repositoryBuilder.name;
        this.metadataPrefix = repositoryBuilder.metadataPrefix;
        this.set = repositoryBuilder.set;
        this.dateStamp = repositoryBuilder.dateStamp;
        this.enabled = repositoryBuilder.enabled;
        this.schedule = repositoryBuilder.schedule;
        this.id = repositoryBuilder.id;
        this.lastHarvest = repositoryBuilder.lastHarvest;
    }


    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public String getSet() {
        return set;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public HarvestSchedule getSchedule() {
        return schedule;
    }

    public Integer getId() {
        return id;
    }

    public Integer getScheduleCode() {
        return schedule.getCode();
    }

    @JsonProperty
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    @JsonProperty
    public void setSet(String set) {
        this.set = set;
    }

    @JsonProperty
    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    @JsonProperty
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty
    public void setSchedule(Integer code) {
        this.schedule = HarvestSchedule.forCode(code);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonIgnore
    public LocalDate getLastHarvest() {
        return lastHarvest;
    }

    public static class RepositoryBuilder {
        private String url;
        private String name;
        private String metadataPrefix;
        private String set;
        private String dateStamp;
        private Boolean enabled;
        private HarvestSchedule schedule;
        private Integer id;
        private LocalDate lastHarvest;

        public RepositoryBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public RepositoryBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public RepositoryBuilder setMetadataPrefix(String metadataPrefix) {
            this.metadataPrefix = metadataPrefix;
            return this;
        }

        public RepositoryBuilder setSet(String set) {
            this.set = set;
            return this;
        }

        public RepositoryBuilder setDateStamp(String dateStamp) {
            this.dateStamp = dateStamp;
            return this;
        }

        public RepositoryBuilder setEnabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public RepositoryBuilder setSchedule(HarvestSchedule schedule) {
            this.schedule = schedule;
            return this;
        }

        public RepositoryBuilder setId(Integer id) {
            this.id = id;
            return this;
        }

        public RepositoryBuilder setLastHarvest(LocalDate lastHarvest) {
            this.lastHarvest = lastHarvest;
            return this;
        }

        public Repository createRepository() {
            return new Repository(this);
        }
    }
}
