package movietracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import movietracker.model.MovieInfo;
import movietracker.service.MovieServiceException.FailureType;

class FakeMovieApiServiceTest {
    private FakeMovieApiService service;

    @BeforeEach
    void setUp() {
        service = new FakeMovieApiService();
    }

    @Test
    void searchReturnsPredefinedApplicationLevelResults() throws MovieServiceException {
        MovieInfo interstellar = movieInfo(157336, "Interstellar");
        MovieInfo inception = movieInfo(27205, "Inception");
        service.setSearchResults(List.of(interstellar, inception));

        List<MovieInfo> results = service.searchMovies("inter");

        assertEquals(List.of(interstellar, inception), results);
    }

    @Test
    void searchResultsAreDefensivelyCopiedAndUnmodifiable() throws MovieServiceException {
        MovieInfo movie = movieInfo(157336, "Interstellar");
        List<MovieInfo> configuredResults = new ArrayList<>();
        configuredResults.add(movie);
        service.setSearchResults(configuredResults);
        configuredResults.clear();

        List<MovieInfo> results = service.searchMovies("Interstellar");

        assertEquals(List.of(movie), results);
        assertThrows(UnsupportedOperationException.class, results::clear);
    }

    @Test
    void searchCanReturnNoResultsWithoutFailure() throws MovieServiceException {
        assertTrue(service.searchMovies("no matches").isEmpty());
    }

    @Test
    void detailsReturnsPredefinedMovieByTmdbId() throws MovieServiceException {
        MovieInfo movie = movieInfo(157336, "Interstellar");
        service.addMovieDetails(movie);

        assertEquals(movie, service.getMovieDetails(157336));
    }

    @Test
    void missingDetailsProduceNotFoundFailure() {
        MovieServiceException exception = assertThrows(
                MovieServiceException.class,
                () -> service.getMovieDetails(999999));

        assertEquals(FailureType.NOT_FOUND, exception.getFailureType());
    }

    @Test
    void configuredSearchFailureIsPropagated() {
        MovieServiceException failure = new MovieServiceException(
                FailureType.TIMEOUT,
                "Movie service timed out");
        service.setSearchFailure(failure);

        MovieServiceException thrown = assertThrows(
                MovieServiceException.class,
                () -> service.searchMovies("Interstellar"));

        assertSame(failure, thrown);
    }

    @Test
    void configuredDetailsFailureIsPropagated() {
        MovieServiceException failure = new MovieServiceException(
                FailureType.AUTHENTICATION,
                "Authentication failed");
        service.setDetailsFailure(failure);

        MovieServiceException thrown = assertThrows(
                MovieServiceException.class,
                () -> service.getMovieDetails(157336));

        assertSame(failure, thrown);
    }

    @Test
    void serviceExceptionPreservesFailureTypeAndCause() {
        RuntimeException cause = new RuntimeException("connection closed");
        MovieServiceException exception = new MovieServiceException(
                FailureType.NETWORK,
                "Unable to reach movie service",
                cause);

        assertEquals(FailureType.NETWORK, exception.getFailureType());
        assertSame(cause, exception.getCause());
    }

    @Test
    void movieInfoRejectsInvalidRequiredIdentityData() {
        assertThrows(IllegalArgumentException.class, () -> movieInfo(0, "Interstellar"));
        assertThrows(IllegalArgumentException.class, () -> movieInfo(157336, "  "));
    }

    private static MovieInfo movieInfo(int tmdbId, String title) {
        return new MovieInfo(
                tmdbId,
                title,
                LocalDate.of(2014, 11, 7),
                "Overview",
                "/poster.jpg",
                8.7);
    }
}
