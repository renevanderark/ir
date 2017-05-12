package nl.kb.dare.model.statuscodes;

public enum ProcessStatus {
    SKIP(1, "skip"),
    PENDING(10, "pending"),
    PROCESSING(20, "processing"),
    FAILED(90, "failure"),
    PROCESSED(100, "processed");

    private final int code;
    private final String status;

    ProcessStatus(int statusCode, String status) {
        this.code = statusCode;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public static ProcessStatus forCode(final int statusCode) {
        for (ProcessStatus s : ProcessStatus.values()) {
            if (s.code == statusCode) {
                return s;
            }
        }
        return null;
    }

    public static ProcessStatus forString(String processStatus) {
        for (ProcessStatus s : ProcessStatus.values()) {
            if (s.status.equalsIgnoreCase(processStatus) || s.name().equals(processStatus)) {
                return s;
            }
        }
        return null;
    }
}
