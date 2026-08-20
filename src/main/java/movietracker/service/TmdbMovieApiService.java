package movietracker.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import movietracker.model.MovieInfo;
import movietracker.service.MovieServiceException.FailureType;

public final class TmdbMovieApiService implements MovieApiService {
    static final String TOKEN_ENVIRONMENT_VARIABLE = "TMDB_API_TOKEN";

    private static final URI DEFAULT_BASE_URI = URI.create("https://api.themoviedb.org/3/");
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;
    private final TmdbMovieMapper mapper;
    private final String apiToken;
    private final URI baseUri;
    private final Duration requestTimeout;

    public TmdbMovieApiService() {
        this(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build(),
                new ObjectMapper(),
                System.getenv(TOKEN_ENVIRONMENT_VARIABLE),
                DEFAULT_BASE_URI,
                REQUEST_TIMEOUT);
    }

    TmdbMovieApiService(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String apiToken,
            URI baseUri,
            Duration requestTimeout) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.mapper = new TmdbMovieMapper(Objects.requireNonNull(objectMapper));
        this.apiToken = apiToken;
        this.baseUri = Objects.requireNonNull(baseUri);
        this.requestTimeout = Objects.requireNonNull(requestTimeout);
    }

    @Override
    public List<MovieInfo> searchMovies(String query) throws MovieServiceException {
        Objects.requireNonNull(query, "query must not be null");
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String responseBody = sendGet("search/movie?query=" + encodedQuery + "&include_adult=false");
        return mapper.parseSearchResults(responseBody);
    }

    @Override
    public MovieInfo getMovieDetails(int tmdbId) throws MovieServiceException {
        if (tmdbId <= 0) {
            throw new IllegalArgumentException("TMDB movie ID must be positive");
        }
        return mapper.parseMovieDetails(sendGet("movie/" + tmdbId));
    }

    private String sendGet(String relativePath) throws MovieServiceException {
        if (apiToken == null || apiToken.isBlank()) {
            throw new MovieServiceException(
                    FailureType.AUTHENTICATION,
                    "TMDB_API_TOKEN is not configured");
        }

        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve(relativePath))
                .timeout(requestTimeout)
                .header("Authorization", "Bearer " + apiToken.strip())
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw exceptionForStatus(response.statusCode());
            }
            return response.body();
        } catch (HttpTimeoutException exception) {
            throw new MovieServiceException(
                    FailureType.TIMEOUT,
                    "TMDB request timed out",
                    exception);
        } catch (IOException exception) {
            throw new MovieServiceException(
                    FailureType.NETWORK,
                    "Unable to communicate with TMDB",
                    exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new MovieServiceException(
                    FailureType.NETWORK,
                    "TMDB request was interrupted",
                    exception);
        }
    }

    static FailureType failureTypeForStatus(int statusCode) {
        return switch (statusCode) {
        case 401, 403 -> FailureType.AUTHENTICATION;
        case 404 -> FailureType.NOT_FOUND;
        case 429 -> FailureType.RATE_LIMIT;
        default -> FailureType.HTTP_ERROR;
        };
    }

    private MovieServiceException exceptionForStatus(int statusCode) {
        return new MovieServiceException(
                failureTypeForStatus(statusCode),
                "TMDB returned HTTP status " + statusCode);
    }
}
