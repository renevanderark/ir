package nl.kb.oaipmh;

public enum OaiStatus {
    DELETED(1, "deleted"),
    AVAILABLE(11, "");

    private final int code;
    private final String status;

    OaiStatus(int statusCode, String status) {
        this.code = statusCode;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public static OaiStatus forCode(final int statusCode) {
        for (OaiStatus s : OaiStatus.values()) {
            if (s.code == statusCode) {
                return s;
            }
        }
        return null;
    }

    public static OaiStatus forString(String oaiStatus) {
        for (OaiStatus s : OaiStatus.values()) {
            if (s.status.equalsIgnoreCase(oaiStatus) || s.name().equals(oaiStatus)) {
                return s;
            }
        }
        return null;
    }
}
