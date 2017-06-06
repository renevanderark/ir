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

    public Repository(String url, String name, String metadataPrefix, String set, String dateStamp, Boolean enabled,
                      HarvestSchedule schedule) {
        this.url = url;
        this.name = name;
        this.metadataPrefix = metadataPrefix;
        this.set = set;
        this.dateStamp = dateStamp;
        this.enabled = enabled;
        this.schedule = schedule;
    }

    public Repository(String url, String name, String metadataPrefix, String set, String dateStamp, Boolean enabled,
                      HarvestSchedule schedule, Integer id, LocalDate lastHarvest) {
        this(url, name, metadataPrefix, set, dateStamp, enabled, schedule);
        this.id = id;
        this.lastHarvest = lastHarvest;
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
}
