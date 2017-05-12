package nl.kb.dare.endpoints;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {

    private final String message;
    private int code;

    ErrorResponse(String message, int statusCode) {
        this.message = message;
        this.code = statusCode;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    @JsonProperty
    public int getCode() {
        return code;
    }
}
