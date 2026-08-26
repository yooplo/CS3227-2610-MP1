package movietracker.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import movietracker.api.HttpTransport.HttpResult;
import movietracker.api.dto.TmdbGenreDto;
import movietracker.api.dto.TmdbMovieDetailsDto;
import movietracker.api.dto.TmdbMovieDto;
import movietracker.api.dto.TmdbSearchResponseDto;
import movietracker.model.Movie;
import movietracker.model.MovieDetails;

/**
 * Read-only client for the TMDB API v3 movie endpoints.
 */
public final class TmdbClient {

    private static final String API_BASE_URL = "https://api.themoviedb.org/3";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final TmdbConfig config;
    private final HttpTransport transport;
    private final ObjectMapper objectMapper;

    /**
     * Creates a production client using environment configuration and the JDK HTTP client.
     *
     * @return configured TMDB client
     * @throws TmdbException if the token is missing
     */
    public static TmdbClient fromEnvironment() throws TmdbException {
        return new TmdbClient(TmdbConfig.fromEnvironment(), new JdkHttpTransport());
    }

    /**
     * Creates a client with an injectable HTTP transport.
     *
     * @param config validated TMDB configuration
     * @param transport HTTP transport
     */
    public TmdbClient(TmdbConfig config, HttpTransport transport) {
        this.config = Objects.requireNonNull(config, "config");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Searches TMDB for movies by title. Blank queries return no results without an HTTP call.
     *
     * @param query movie-title query
     * @return mapped search results
     * @throws TmdbException if the request or response fails
     */
    public List<Movie> searchMovies(String query) throws TmdbException {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        URI uri = URI.create(API_BASE_URL
                + "/search/movie?query=" + encodedQuery
                + "&include_adult=false&language=en-US&page=1");
        String responseBody = executeGet(uri);

        try {
            TmdbSearchResponseDto response = objectMapper.readValue(
                    responseBody, TmdbSearchResponseDto.class);
            if (response == null || response.results() == null) {
                return List.of();
            }
            return response.results().stream()
                    .map(TmdbClient::toMovie)
                    .toList();
        } catch (IOException | RuntimeException exception) {
            throw invalidResponse(exception);
        }
    }

    /**
     * Fetches detailed information for one TMDB movie.
     *
     * @param tmdbId positive TMDB movie ID
     * @return mapped movie details
     * @throws TmdbException if the request or response fails
     */
    public MovieDetails getMovieDetails(int tmdbId) throws TmdbException {
        if (tmdbId <= 0) {
            throw new IllegalArgumentException("TMDB movie ID must be positive");
        }

        URI uri = URI.create(API_BASE_URL + "/movie/" + tmdbId + "?language=en-US");
        String responseBody = executeGet(uri);

        try {
            TmdbMovieDetailsDto response = objectMapper.readValue(
                    responseBody, TmdbMovieDetailsDto.class);
            if (response == null || response.id() == null || response.id() != tmdbId) {
                throw new IllegalArgumentException("TMDB detail response has an unexpected movie ID");
            }
            Movie movie = new Movie(
                    response.id(),
                    response.title(),
                    parseOptionalDate(response.releaseDate()),
                    response.posterPath());
            List<String> genres = response.genres() == null
                    ? List.of()
                    : response.genres().stream().map(TmdbGenreDto::name).toList();
            return new MovieDetails(
                    movie,
                    response.overview(),
                    response.runtime(),
                    genres,
                    response.backdropPath(),
                    response.voteAverage());
        } catch (IOException | RuntimeException exception) {
            throw invalidResponse(exception);
        }
    }

    private String executeGet(URI uri) throws TmdbException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", config.authorizationHeaderValue())
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResult response = transport.send(request);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw TmdbException.forHttpStatus(response.statusCode());
            }
            return response.body();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw TmdbException.forInterruptedRequest(exception);
        } catch (HttpTimeoutException exception) {
            throw TmdbException.forTimeout(exception);
        } catch (IOException exception) {
            throw TmdbException.forNetworkFailure(exception);
        }
    }

    private static Movie toMovie(TmdbMovieDto response) {
        Objects.requireNonNull(response, "movie response");
        return new Movie(
                Objects.requireNonNull(response.id(), "movie ID"),
                response.title(),
                parseOptionalDate(response.releaseDate()),
                response.posterPath());
    }

    private static LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private static TmdbException invalidResponse(Exception cause) {
        return TmdbException.forInvalidResponse(cause);
    }
}
