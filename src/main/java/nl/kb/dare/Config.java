package nl.kb.dare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import nl.kb.dare.config.FileStorageFactory;

class Config extends Configuration {
    private DataSourceFactory database;
    private FileStorageFactory fileStorageFactory;

    @JsonProperty
    private String hostName;
    @JsonProperty
    private String databaseProvider;
    @JsonProperty
    private String numbersEndpoint;


    @JsonProperty("database")
    DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.database = dataSourceFactory;
    }

    @JsonProperty("fileStorage")
    public FileStorageFactory getFileStorageFactory() {
        return fileStorageFactory;
    }

    @JsonProperty("fileStorage")
    public void setFileStorageFactory(FileStorageFactory fileStorageFactory) {
        this.fileStorageFactory = fileStorageFactory;
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
