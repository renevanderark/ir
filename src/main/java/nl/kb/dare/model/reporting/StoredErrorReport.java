package nl.kb.dare.model.reporting;

import java.util.Date;

public class StoredErrorReport {


    private final String kbObjId;
    private final String oaiId;

    @Override
    public String toString() {
        return "StoredErrorReport{" +
                "kbObjId='" + kbObjId + '\'' +
                ", oaiId='" + oaiId + '\'' +
                ", url='" + url + '\'' +
                ", message='" + message + '\'' +
                ", statusCode=" + statusCode +
                ", repositoryName='" + repositoryName + '\'' +
                ", tsCreate=" + tsCreate +
                '}';
    }

    private final String url;
    private final String message;
    private final Integer statusCode;
    private final String repositoryName;
    private final Date tsCreate;

    public StoredErrorReport(String kbObjId, String oaiId, String url, String message,
                             Integer statusCode, String repositoryName, Date tsCreate) {

        this.kbObjId = kbObjId;
        this.oaiId = oaiId;
        this.url = url;
        this.message = message;
        this.statusCode = statusCode;
        this.repositoryName = repositoryName;
        this.tsCreate = tsCreate;
    }
}
