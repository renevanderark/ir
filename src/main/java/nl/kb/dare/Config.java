package nl.kb.dare;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import nl.kb.dare.config.FileStorageFactory;
import nl.kb.dare.config.MailerFactory;

class Config extends Configuration {
    private DataSourceFactory database;
    private FileStorageFactory fileStorageFactory;
    private MailerFactory mailerFactory;

    @JsonProperty
    private String hostName;

    @JsonProperty
    private String numbersEndpoint;

    @JsonProperty
    private Integer maxParallelHarvests;

    @JsonProperty
    private Integer maxParallelDownloads;

    @JsonProperty
    private Long downloadQueueFillDelayMs;

    @JsonProperty
    private Boolean authEnabled;
    @JsonProperty
    private String kbAutLocation;

    @JsonProperty
    private Integer maxConsecutiveDownloadFailures = 10;


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

    @JsonProperty("mailer")
    public MailerFactory getMailerFactory() { return mailerFactory; }

    @JsonProperty("mailer")
    public void setMailerFactory(MailerFactory mailerFactory) {
        this.mailerFactory = mailerFactory;
    }


    public String getHostName() {
        return hostName;
    }

    public String getNumbersEndpoint() {
        return numbersEndpoint;
    }

    public Integer getMaxParallelHarvests() {
        return maxParallelHarvests;
    }

    public Integer getMaxParallelDownloads() {
        return maxParallelDownloads;
    }

    public Long getDownloadQueueFillDelayMs() {
        return downloadQueueFillDelayMs;
    }

    public boolean getAuthEnabled() {
        return authEnabled;
    }

    public String getKbAutLocation() {
        return kbAutLocation;
    }

    public Integer getMaxConsecutiveDownloadFailures() {
        return maxConsecutiveDownloadFailures;
    }
}
