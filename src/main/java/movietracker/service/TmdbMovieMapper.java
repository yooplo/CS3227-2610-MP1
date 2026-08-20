package movietracker.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import movietracker.model.MovieInfo;
import movietracker.service.MovieServiceException.FailureType;

final class TmdbMovieMapper {
    private final ObjectMapper objectMapper;

    TmdbMovieMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    List<MovieInfo> parseSearchResults(String responseBody) throws MovieServiceException {
        JsonNode root = parseJson(responseBody);
        JsonNode results = root.get("results");
        if (results == null || !results.isArray()) {
            throw invalidResponse("TMDB search response is missing a results array", null);
        }

        List<MovieInfo> movies = new ArrayList<>();
        for (JsonNode result : results) {
            movies.add(mapMovie(result));
        }
        return List.copyOf(movies);
    }

    MovieInfo parseMovieDetails(String responseBody) throws MovieServiceException {
        return mapMovie(parseJson(responseBody));
    }

    private JsonNode parseJson(String responseBody) throws MovieServiceException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root == null || !root.isObject()) {
                throw invalidResponse("TMDB response must be a JSON object", null);
            }
            return root;
        } catch (JsonProcessingException exception) {
            throw invalidResponse("TMDB returned malformed JSON", exception);
        }
    }

    private MovieInfo mapMovie(JsonNode movieNode) throws MovieServiceException {
        if (movieNode == null || !movieNode.isObject()) {
            throw invalidResponse("TMDB movie entry must be a JSON object", null);
        }

        int tmdbId = requiredPositiveId(movieNode.get("id"));
        String title = requiredText(movieNode.get("title"), "title");
        LocalDate releaseDate = optionalDate(movieNode.get("release_date"));
        String overview = optionalText(movieNode.get("overview"), "overview");
        String posterPath = optionalText(movieNode.get("poster_path"), "poster_path");
        Double externalRating = optionalRating(movieNode.get("vote_average"));

        try {
            return new MovieInfo(
                    tmdbId,
                    title,
                    releaseDate,
                    overview,
                    posterPath,
                    externalRating);
        } catch (IllegalArgumentException exception) {
            throw invalidResponse("TMDB returned invalid movie data", exception);
        }
    }

    private int requiredPositiveId(JsonNode idNode) throws MovieServiceException {
        if (idNode == null || !idNode.isIntegralNumber()
                || !idNode.canConvertToInt() || idNode.intValue() <= 0) {
            throw invalidResponse("TMDB movie is missing a valid positive id", null);
        }
        return idNode.intValue();
    }

    private String requiredText(JsonNode node, String fieldName) throws MovieServiceException {
        String value = optionalText(node, fieldName);
        if (value == null || value.isBlank()) {
            throw invalidResponse("TMDB movie is missing a valid " + fieldName, null);
        }
        return value;
    }

    private String optionalText(JsonNode node, String fieldName) throws MovieServiceException {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isTextual()) {
            throw invalidResponse("TMDB field " + fieldName + " must be text or null", null);
        }

        String value = node.textValue();
        return value.isBlank() ? null : value;
    }

    private LocalDate optionalDate(JsonNode node) throws MovieServiceException {
        String value = optionalText(node, "release_date");
        if (value == null) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw invalidResponse("TMDB release_date is not a valid ISO date", exception);
        }
    }

    private Double optionalRating(JsonNode node) throws MovieServiceException {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isNumber()) {
            throw invalidResponse("TMDB vote_average must be numeric or null", null);
        }

        double rating = node.doubleValue();
        if (!Double.isFinite(rating) || rating < 0.0 || rating > 10.0) {
            throw invalidResponse("TMDB vote_average must be between 0 and 10", null);
        }
        return rating;
    }

    private MovieServiceException invalidResponse(String message, Throwable cause) {
        return new MovieServiceException(FailureType.INVALID_RESPONSE, message, cause);
    }
}
