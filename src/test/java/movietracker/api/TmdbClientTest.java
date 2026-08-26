package movietracker.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import movietracker.api.HttpTransport.HttpResult;
import movietracker.model.Movie;
import movietracker.model.MovieDetails;

class TmdbClientTest {

    private static final String TEST_TOKEN = "test-read-access-token";

    @Test
    void searchMovies_success_mapsMoviesAndBuildsAuthenticatedRequest() throws Exception {
        CapturingTransport transport = new CapturingTransport(
                new HttpResult(200, readFixture("tmdb-search-response.json")));
        TmdbClient client = createClient(transport);

        List<Movie> movies = client.searchMovies("Fight Club");

        assertEquals(1, movies.size());
        Movie movie = movies.getFirst();
        assertEquals(550, movie.getTmdbId());
        assertEquals("Fight Club", movie.getTitle());
        assertEquals(LocalDate.of(1999, 10, 15), movie.getReleaseDate().orElseThrow());
        assertEquals("/poster.jpg", movie.getPosterPath().orElseThrow());
        assertTrue(transport.request.uri().toString().contains("query=Fight%20Club"));
        assertEquals("Bearer " + TEST_TOKEN,
                transport.request.headers().firstValue("Authorization").orElseThrow());
        assertEquals("application/json",
                transport.request.headers().firstValue("Accept").orElseThrow());
    }

    @Test
    void getMovieDetails_success_mapsDetailedFields() throws Exception {
        TmdbClient client = createClient(request ->
                new HttpResult(200, readFixture("tmdb-details-response.json")));

        MovieDetails details = client.getMovieDetails(550);

        assertEquals(550, details.getMovie().getTmdbId());
        assertEquals("Fight Club", details.getMovie().getTitle());
        assertEquals(LocalDate.of(1999, 10, 15),
                details.getMovie().getReleaseDate().orElseThrow());
        assertEquals("/poster.jpg", details.getMovie().getPosterPath().orElseThrow());
        assertEquals("An insomniac meets a soap maker.", details.getOverview().orElseThrow());
        assertEquals(139, details.getRuntimeMinutes().orElseThrow());
        assertEquals(List.of("Drama", "Thriller"), details.getGenres());
        assertEquals("/backdrop.jpg", details.getBackdropPath().orElseThrow());
        assertEquals(8.4, details.getTmdbVoteAverage().orElseThrow());
    }

    @Test
    void searchMovies_emptyResults_returnsEmptyList() throws Exception {
        TmdbClient client = createClient(request -> new HttpResult(200, "{\"results\": []}"));

        assertTrue(client.searchMovies("No Such Movie").isEmpty());
    }

    @Test
    void searchMovies_blankQuery_returnsEmptyWithoutHttpCall() throws Exception {
        int[] calls = {0};
        TmdbClient client = createClient(request -> {
            calls[0]++;
            return new HttpResult(200, "{\"results\": []}");
        });

        assertTrue(client.searchMovies("   ").isEmpty());
        assertEquals(0, calls[0]);
    }

    @Test
    void request_httpError_throwsTmdbExceptionWithoutResponseBody() throws Exception {
        TmdbClient client = createClient(request ->
                new HttpResult(401, "response body must not be exposed"));

        TmdbException exception = assertThrows(
                TmdbException.class, () -> client.searchMovies("Fight Club"));

        assertEquals(401, exception.getStatusCode().orElseThrow());
        assertEquals(TmdbErrorCategory.HTTP_ERROR, exception.getCategory());
        assertFalse(exception.getMessage().contains("response body"));
    }

    @Test
    void request_networkFailure_throwsTmdbException() throws Exception {
        TmdbClient client = createClient(request -> {
            throw new IOException("offline");
        });

        TmdbException exception = assertThrows(
                TmdbException.class, () -> client.searchMovies("Fight Club"));

        assertEquals(TmdbErrorCategory.NETWORK, exception.getCategory());
        assertEquals("Could not reach TMDB", exception.getMessage());
        assertEquals("offline", exception.getCause().getMessage());
    }

    @Test
    void request_timeout_throwsCategorizedTmdbExceptionWithCause() throws Exception {
        HttpTimeoutException timeout = new HttpTimeoutException("timed out");
        TmdbClient client = createClient(request -> {
            throw timeout;
        });

        TmdbException exception = assertThrows(
                TmdbException.class, () -> client.searchMovies("Fight Club"));

        assertEquals(TmdbErrorCategory.TIMEOUT, exception.getCategory());
        assertEquals("TMDB request timed out", exception.getMessage());
        assertSame(timeout, exception.getCause());
    }

    @Test
    void searchMovies_malformedJson_throwsTmdbException() throws Exception {
        TmdbClient client = createClient(request -> new HttpResult(200, "{ invalid"));

        TmdbException exception = assertThrows(
                TmdbException.class, () -> client.searchMovies("Fight Club"));

        assertEquals(TmdbErrorCategory.INVALID_RESPONSE, exception.getCategory());
        assertEquals("TMDB returned an invalid response", exception.getMessage());
    }

    @Test
    void config_missingOrBlankToken_throwsWithoutExposingToken() {
        TmdbException missing = assertThrows(TmdbException.class, () -> new TmdbConfig(null));
        TmdbException blank = assertThrows(TmdbException.class, () -> new TmdbConfig("   "));

        assertEquals(TmdbErrorCategory.MISSING_TOKEN, missing.getCategory());
        assertEquals(TmdbErrorCategory.MISSING_TOKEN, blank.getCategory());
        assertTrue(missing.getMessage().contains(TmdbConfig.TOKEN_ENVIRONMENT_VARIABLE));
        assertTrue(blank.getMessage().contains(TmdbConfig.TOKEN_ENVIRONMENT_VARIABLE));
    }

    @Test
    void lazyConfig_missingTokenFailsRequestWithoutCallingTransport() {
        int[] calls = {0};
        TmdbClient client = new TmdbClient(
                () -> {
                    throw TmdbException.forMissingToken();
                },
                request -> {
                    calls[0]++;
                    return new HttpResult(200, "{\"results\": []}");
                });

        TmdbException exception = assertThrows(
                TmdbException.class, () -> client.searchMovies("Fight Club"));

        assertEquals(TmdbErrorCategory.MISSING_TOKEN, exception.getCategory());
        assertEquals(0, calls[0]);
    }

    @Test
    void optionalNullFields_mapToEmptyDomainValues() throws Exception {
        String response = """
                {
                  "id": 550,
                  "title": "Fight Club",
                  "release_date": null,
                  "runtime": null,
                  "genres": null,
                  "overview": null,
                  "poster_path": null,
                  "backdrop_path": null,
                  "vote_average": null
                }
                """;
        TmdbClient client = createClient(request -> new HttpResult(200, response));

        MovieDetails details = client.getMovieDetails(550);

        assertTrue(details.getMovie().getReleaseDate().isEmpty());
        assertTrue(details.getMovie().getPosterPath().isEmpty());
        assertTrue(details.getRuntimeMinutes().isEmpty());
        assertTrue(details.getGenres().isEmpty());
        assertTrue(details.getOverview().isEmpty());
        assertTrue(details.getBackdropPath().isEmpty());
        assertTrue(details.getTmdbVoteAverage().isEmpty());
    }

    @Test
    void getMovieDetails_invalidResponseData_throwsTmdbException() throws Exception {
        String response = """
                {
                  "id": 550,
                  "title": "Fight Club",
                  "runtime": -1,
                  "genres": []
                }
                """;
        TmdbClient client = createClient(request -> new HttpResult(200, response));

        TmdbException exception = assertThrows(
                TmdbException.class, () -> client.getMovieDetails(550));
        assertEquals(TmdbErrorCategory.INVALID_RESPONSE, exception.getCategory());
    }

    @Test
    void getMovieDetails_invalidRequestedId_rejectsWithoutHttpCall() throws Exception {
        int[] calls = {0};
        TmdbClient client = createClient(request -> {
            calls[0]++;
            return new HttpResult(200, "{}");
        });

        assertThrows(IllegalArgumentException.class, () -> client.getMovieDetails(0));
        assertEquals(0, calls[0]);
    }

    private static TmdbClient createClient(HttpTransport transport) throws TmdbException {
        return new TmdbClient(new TmdbConfig(TEST_TOKEN), transport);
    }

    private static String readFixture(String name) throws IOException {
        String path = "/fixtures/" + name;
        try (InputStream stream = TmdbClientTest.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Missing test fixture: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static final class CapturingTransport implements HttpTransport {

        private final HttpResult result;
        private HttpRequest request;

        private CapturingTransport(HttpResult result) {
            this.result = result;
        }

        @Override
        public HttpResult send(HttpRequest request) {
            this.request = request;
            return result;
        }
    }
}
