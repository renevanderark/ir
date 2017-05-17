package nl.kb.dare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

class Config extends Configuration {
    private DataSourceFactory database;

    @JsonProperty
    private String hostName;
    @JsonProperty
    private String databaseProvider;

    @JsonProperty
    private String numbersEndpoint;

    @JsonProperty
    private Boolean inSampleMode = false;

    @JsonProperty("database")
    DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    Boolean getInSampleMode() {
        return inSampleMode;
    }

    String getDatabaseProvider() {
        return databaseProvider;
    }

    public String getHostName() {
        return hostName;
    }

    public String getNumbersEndpoint() {
        return numbersEndpoint;
    }
}
