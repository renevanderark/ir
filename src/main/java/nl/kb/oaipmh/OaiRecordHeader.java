package nl.kb.oaipmh;

import org.apache.commons.codec.digest.DigestUtils;

public class OaiRecordHeader {
    private String identifier;
    private String dateStamp;
    private OaiStatus oaiStatus;

    public OaiRecordHeader() {
    }

    public String getFingerprint() {
        return DigestUtils.sha1Hex(identifier + "|" + dateStamp);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    public OaiStatus getOaiStatus() {
        return oaiStatus;
    }

    public void setOaiStatus(OaiStatus oaiStatus) {
        this.oaiStatus = oaiStatus;
    }
}
