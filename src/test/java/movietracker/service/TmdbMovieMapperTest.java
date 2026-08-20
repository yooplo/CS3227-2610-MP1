package movietracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import movietracker.model.MovieInfo;
import movietracker.service.MovieServiceException.FailureType;

class TmdbMovieMapperTest {
    private TmdbMovieMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TmdbMovieMapper(new ObjectMapper());
    }

    @Test
    void mapsSearchResultsToMovieInfo() throws MovieServiceException {
        String json = """
                {
                  "page": 1,
                  "results": [
                    {
                      "id": 157336,
                      "title": "Interstellar",
                      "release_date": "2014-11-05",
                      "overview": "Explorers travel through a wormhole.",
                      "poster_path": "/poster.jpg",
                      "vote_average": 8.5,
                      "ignored_tmdb_field": true
                    }
                  ]
                }
                """;

        List<MovieInfo> results = mapper.parseSearchResults(json);

        assertEquals(1, results.size());
        MovieInfo movie = results.getFirst();
        assertEquals(157336, movie.tmdbId());
        assertEquals("Interstellar", movie.title());
        assertEquals(LocalDate.of(2014, 11, 5), movie.releaseDate());
        assertEquals("Explorers travel through a wormhole.", movie.overview());
        assertEquals("/poster.jpg", movie.posterPath());
        assertEquals(8.5, movie.externalRating());
    }

    @Test
    void emptySearchResultsReturnEmptyList() throws MovieServiceException {
        assertTrue(mapper.parseSearchResults("{\"results\":[]}").isEmpty());
    }

    @Test
    void missingNullableMetadataMapsToNull() throws MovieServiceException {
        MovieInfo movie = mapper.parseMovieDetails("""
                {
                  "id": 157336,
                  "title": "Interstellar"
                }
                """);

        assertNull(movie.releaseDate());
        assertNull(movie.overview());
        assertNull(movie.posterPath());
        assertNull(movie.externalRating());
    }

    @Test
    void nullAndBlankOptionalMetadataMapToNull() throws MovieServiceException {
        MovieInfo movie = mapper.parseMovieDetails("""
                {
                  "id": 157336,
                  "title": "Interstellar",
                  "release_date": "",
                  "overview": " ",
                  "poster_path": null,
                  "vote_average": null
                }
                """);

        assertNull(movie.releaseDate());
        assertNull(movie.overview());
        assertNull(movie.posterPath());
        assertNull(movie.externalRating());
    }

    @Test
    void malformedJsonProducesInvalidResponseFailure() {
        assertInvalidResponse(() -> mapper.parseSearchResults("{not-json"));
        assertInvalidResponse(() -> mapper.parseSearchResults(null));
    }

    @Test
    void missingResultsArrayProducesInvalidResponseFailure() {
        assertInvalidResponse(() -> mapper.parseSearchResults("{\"page\":1}"));
        assertInvalidResponse(() -> mapper.parseSearchResults("{\"results\":{}}"));
    }

    @Test
    void missingRequiredMovieFieldsProduceInvalidResponseFailure() {
        assertInvalidResponse(() -> mapper.parseMovieDetails("{\"title\":\"Interstellar\"}"));
        assertInvalidResponse(() -> mapper.parseMovieDetails("{\"id\":157336}"));
        assertInvalidResponse(() -> mapper.parseMovieDetails(
                "{\"id\":-1,\"title\":\"Interstellar\"}"));
        assertInvalidResponse(() -> mapper.parseMovieDetails(
                "{\"id\":1.5,\"title\":\"Interstellar\"}"));
    }

    @Test
    void invalidOptionalFieldTypesProduceInvalidResponseFailure() {
        assertInvalidResponse(() -> mapper.parseMovieDetails("""
                {"id":157336,"title":"Interstellar","overview":42}
                """));
        assertInvalidResponse(() -> mapper.parseMovieDetails("""
                {"id":157336,"title":"Interstellar","vote_average":"8.5"}
                """));
    }

    @Test
    void invalidReleaseDateProducesInvalidResponseFailure() {
        assertInvalidResponse(() -> mapper.parseMovieDetails("""
                {"id":157336,"title":"Interstellar","release_date":"not-a-date"}
                """));
    }

    @Test
    void ratingOutsideTmdbScaleProducesInvalidResponseFailure() {
        assertInvalidResponse(() -> mapper.parseMovieDetails("""
                {"id":157336,"title":"Interstellar","vote_average":10.1}
                """));
    }

    private static void assertInvalidResponse(ThrowingOperation operation) {
        MovieServiceException exception = assertThrows(MovieServiceException.class, operation::run);
        assertEquals(FailureType.INVALID_RESPONSE, exception.getFailureType());
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run() throws MovieServiceException;
    }
}
