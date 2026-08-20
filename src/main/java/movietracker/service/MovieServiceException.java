package movietracker.service;

import java.util.Objects;

public class MovieServiceException extends Exception {
    public enum FailureType {
        NETWORK,
        TIMEOUT,
        INVALID_RESPONSE,
        AUTHENTICATION,
        RATE_LIMIT,
        NOT_FOUND,
        HTTP_ERROR
    }

    private final FailureType failureType;

    public MovieServiceException(FailureType failureType, String message) {
        super(message);
        this.failureType = Objects.requireNonNull(failureType, "failureType must not be null");
    }

    public MovieServiceException(FailureType failureType, String message, Throwable cause) {
        super(message, cause);
        this.failureType = Objects.requireNonNull(failureType, "failureType must not be null");
    }

    public FailureType getFailureType() {
        return failureType;
    }
}
