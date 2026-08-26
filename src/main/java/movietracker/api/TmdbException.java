package movietracker.api;

import java.util.OptionalInt;
import java.util.Objects;

/**
 * Signals a TMDB configuration, transport, HTTP, or response-mapping failure.
 */
public class TmdbException extends Exception {

    private final TmdbErrorCategory category;
    private final Integer statusCode;

    private TmdbException(TmdbErrorCategory category, String message,
                          Throwable cause, Integer statusCode) {
        super(message, cause);
        this.category = Objects.requireNonNull(category, "category");
        this.statusCode = statusCode;
    }

    static TmdbException forMissingToken() {
        return new TmdbException(
                TmdbErrorCategory.MISSING_TOKEN,
                "TMDB API token is missing; set " + TmdbConfig.TOKEN_ENVIRONMENT_VARIABLE,
                null,
                null);
    }

    static TmdbException forHttpStatus(int statusCode) {
        return new TmdbException(
                TmdbErrorCategory.HTTP_ERROR,
                "TMDB request failed with HTTP status " + statusCode,
                null,
                statusCode);
    }

    static TmdbException forNetworkFailure(Throwable cause) {
        return new TmdbException(
                TmdbErrorCategory.NETWORK, "Could not reach TMDB", cause, null);
    }

    static TmdbException forTimeout(Throwable cause) {
        return new TmdbException(
                TmdbErrorCategory.TIMEOUT, "TMDB request timed out", cause, null);
    }

    static TmdbException forInterruptedRequest(Throwable cause) {
        return new TmdbException(
                TmdbErrorCategory.INTERRUPTED, "TMDB request was interrupted", cause, null);
    }

    static TmdbException forInvalidResponse(Throwable cause) {
        return new TmdbException(
                TmdbErrorCategory.INVALID_RESPONSE,
                "TMDB returned an invalid response",
                cause,
                null);
    }

    public TmdbErrorCategory getCategory() {
        return category;
    }

    public OptionalInt getStatusCode() {
        return statusCode == null ? OptionalInt.empty() : OptionalInt.of(statusCode);
    }
}
