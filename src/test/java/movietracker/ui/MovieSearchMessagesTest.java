package movietracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import movietracker.service.MovieServiceException;
import movietracker.service.MovieServiceException.FailureType;

class MovieSearchMessagesTest {
    @Test
    void mapsServiceFailuresToUserFacingMessages() {
        assertEquals(
                "Unable to connect to TMDB. Check your internet connection and try again.",
                messageFor(FailureType.NETWORK));
        assertEquals(
                "The movie search timed out. Please try again.",
                messageFor(FailureType.TIMEOUT));
        assertEquals(
                "TMDB access is not configured or the token is invalid. "
                        + "Set TMDB_API_TOKEN and restart Movie Tracker.",
                messageFor(FailureType.AUTHENTICATION));
        assertEquals(
                "Too many movie searches were requested. Please wait and try again.",
                messageFor(FailureType.RATE_LIMIT));
        assertEquals(
                "TMDB returned an unexpected response. Please try again later.",
                messageFor(FailureType.INVALID_RESPONSE));
        assertEquals(
                "TMDB could not complete the search. Please try again later.",
                messageFor(FailureType.HTTP_ERROR));
        assertEquals(
                "The requested movie could not be found.",
                messageFor(FailureType.NOT_FOUND));
    }

    @Test
    void mapsUnexpectedFailureWithoutExposingDetails() {
        assertEquals(
                "Unable to search for movies. Please try again.",
                MovieSearchMessages.forFailure(new RuntimeException("sensitive detail")));
    }

    private static String messageFor(FailureType failureType) {
        return MovieSearchMessages.forFailure(
                new MovieServiceException(failureType, "low-level detail"));
    }
}
