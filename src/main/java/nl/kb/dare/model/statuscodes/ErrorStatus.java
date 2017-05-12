package nl.kb.dare.model.statuscodes;

public enum ErrorStatus {
    // HTTP ERRORS
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),

    // PROCESSING ERRORS
    UPDATED_DURING_PROCESSING(1001, "record was updated by provider during processing"),
    DELETED_DURING_PROCESSING(1002, "record was deleted by provider during processing"),
    XML_PARSING_ERROR(1003, "failed to parse XML"),
    IO_EXCEPTION(1004, "I/O exception occurred"),
    NO_RESOURCES(1005, "record ships no object files");

    private final int code;
    private final String status;

    ErrorStatus(int statusCode, String status) {

        this.code = statusCode;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public static ErrorStatus forCode(final int statusCode) {
        for (ErrorStatus s : ErrorStatus.values()) {
            if (s.code == statusCode) {
                return s;
            }
        }
        return null;
    }

    public static ErrorStatus forString(String errorStatus) {
        for (ErrorStatus s : ErrorStatus.values()) {
            if (s.status.equalsIgnoreCase(errorStatus) || s.name().equals(errorStatus)) {
                return s;
            }
        }
        return null;
    }
}
