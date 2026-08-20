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
                searchMessageFor(FailureType.NETWORK));
        assertEquals(
                "The movie search timed out. Please try again.",
                searchMessageFor(FailureType.TIMEOUT));
        assertEquals(
                "TMDB access is not configured or the token is invalid. "
                        + "Set TMDB_API_TOKEN and restart Movie Tracker.",
                searchMessageFor(FailureType.AUTHENTICATION));
        assertEquals(
                "Too many movie searches were requested. Please wait and try again.",
                searchMessageFor(FailureType.RATE_LIMIT));
        assertEquals(
                "TMDB returned an unexpected response. Please try again later.",
                searchMessageFor(FailureType.INVALID_RESPONSE));
        assertEquals(
                "TMDB could not complete the search. Please try again later.",
                searchMessageFor(FailureType.HTTP_ERROR));
        assertEquals(
                "The requested movie could not be found.",
                searchMessageFor(FailureType.NOT_FOUND));
    }

    @Test
    void mapsUnexpectedFailureWithoutExposingDetails() {
        assertEquals(
                "Unable to search for movies. Please try again.",
                MovieSearchMessages.forSearchFailure(new RuntimeException("sensitive detail")));
    }

    @Test
    void mapsDetailServiceFailuresToUserFacingMessages() {
        assertEquals(
                "Unable to connect to TMDB while loading movie details. "
                        + "Check your internet connection and try again.",
                detailsMessageFor(FailureType.NETWORK));
        assertEquals("Loading movie details timed out. Please try again.",
                detailsMessageFor(FailureType.TIMEOUT));
        assertEquals("TMDB access is not configured or the token is invalid. "
                        + "Set TMDB_API_TOKEN and restart Movie Tracker.",
                detailsMessageFor(FailureType.AUTHENTICATION));
        assertEquals("Too many TMDB requests were made. Please wait and try again.",
                detailsMessageFor(FailureType.RATE_LIMIT));
        assertEquals("Movie details could not be found.",
                detailsMessageFor(FailureType.NOT_FOUND));
        assertEquals("TMDB returned unexpected movie details. Please try again later.",
                detailsMessageFor(FailureType.INVALID_RESPONSE));
        assertEquals("TMDB could not load the movie details. Please try again later.",
                detailsMessageFor(FailureType.HTTP_ERROR));
    }

    @Test
    void mapsUnexpectedDetailFailureWithoutExposingDetails() {
        assertEquals(
                "Unable to load movie details. Please try again.",
                MovieSearchMessages.forDetailsFailure(new RuntimeException("sensitive detail")));
    }

    private static String searchMessageFor(FailureType failureType) {
        return MovieSearchMessages.forSearchFailure(
                new MovieServiceException(failureType, "low-level detail"));
    }

    private static String detailsMessageFor(FailureType failureType) {
        return MovieSearchMessages.forDetailsFailure(
                new MovieServiceException(failureType, "low-level detail"));
    }
}
