package movietracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import movietracker.service.MovieServiceException.FailureType;

class TmdbMovieApiServiceTest {
    @Test
    void mapsAuthenticationStatuses() {
        assertEquals(FailureType.AUTHENTICATION, TmdbMovieApiService.failureTypeForStatus(401));
        assertEquals(FailureType.AUTHENTICATION, TmdbMovieApiService.failureTypeForStatus(403));
    }

    @Test
    void mapsNotFoundStatus() {
        assertEquals(FailureType.NOT_FOUND, TmdbMovieApiService.failureTypeForStatus(404));
    }

    @Test
    void mapsRateLimitStatus() {
        assertEquals(FailureType.RATE_LIMIT, TmdbMovieApiService.failureTypeForStatus(429));
    }

    @Test
    void mapsOtherFailureStatusesToHttpError() {
        assertEquals(FailureType.HTTP_ERROR, TmdbMovieApiService.failureTypeForStatus(400));
        assertEquals(FailureType.HTTP_ERROR, TmdbMovieApiService.failureTypeForStatus(500));
        assertEquals(FailureType.HTTP_ERROR, TmdbMovieApiService.failureTypeForStatus(503));
    }

    @Test
    void missingTokenFailsBeforeAnyNetworkRequest() {
        TmdbMovieApiService service = new TmdbMovieApiService(
                HttpClient.newHttpClient(),
                new ObjectMapper(),
                "  ",
                URI.create("https://example.invalid/"),
                Duration.ofSeconds(1));

        MovieServiceException exception = assertThrows(
                MovieServiceException.class,
                () -> service.searchMovies("Interstellar"));

        assertEquals(FailureType.AUTHENTICATION, exception.getFailureType());
    }

    @Test
    void invalidMovieIdIsRejectedBeforeAnyNetworkRequest() {
        TmdbMovieApiService service = new TmdbMovieApiService(
                HttpClient.newHttpClient(),
                new ObjectMapper(),
                "test-token",
                URI.create("https://example.invalid/"),
                Duration.ofSeconds(1));

        assertThrows(IllegalArgumentException.class, () -> service.getMovieDetails(0));
    }
}
