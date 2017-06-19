package nl.kb.dare.model.reporting;

public class StoredErrorReport {

    private final int statusCode;
    private final String message;
    private final String url;
    private final String stackTrace;

    StoredErrorReport(int statusCode, String message, String url, String stackTrace) {

        this.statusCode = statusCode;
        this.message = message;
        this.url = url;
        this.stackTrace = stackTrace;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }

    public String getStackTrace() {
        return stackTrace;
    }

}

