package movietracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Immutable detailed information about a movie.
 */
public final class MovieDetails {

    private static final double MINIMUM_VOTE_AVERAGE = 0.0;
    private static final double MAXIMUM_VOTE_AVERAGE = 10.0;

    private final Movie movie;
    private final String overview;
    private final Integer runtimeMinutes;
    private final List<String> genres;
    private final String backdropPath;
    private final Double tmdbVoteAverage;

    /**
     * Creates detailed movie information.
     *
     * @param movie common movie information and TMDB identity
     * @param overview full overview, or {@code null} when unavailable
     * @param runtimeMinutes non-negative runtime, or {@code null} when unavailable
     * @param genres genre names; may be empty but not {@code null}
     * @param backdropPath TMDB backdrop path, or {@code null} when unavailable
     * @param tmdbVoteAverage TMDB vote average from 0 to 10, or {@code null} when unavailable
     */
    public MovieDetails(Movie movie, String overview, Integer runtimeMinutes, List<String> genres,
                        String backdropPath, Double tmdbVoteAverage) {
        this.movie = Objects.requireNonNull(movie, "movie");
        this.overview = normalizeOptionalText(overview);
        this.runtimeMinutes = validateRuntime(runtimeMinutes);
        this.genres = copyAndValidateGenres(genres);
        this.backdropPath = normalizeOptionalText(backdropPath);
        this.tmdbVoteAverage = validateVoteAverage(tmdbVoteAverage);
    }

    public Movie getMovie() {
        return movie;
    }

    public Optional<String> getOverview() {
        return Optional.ofNullable(overview);
    }

    public OptionalInt getRuntimeMinutes() {
        return runtimeMinutes == null ? OptionalInt.empty() : OptionalInt.of(runtimeMinutes);
    }

    public List<String> getGenres() {
        return genres;
    }

    public Optional<String> getBackdropPath() {
        return Optional.ofNullable(backdropPath);
    }

    public OptionalDouble getTmdbVoteAverage() {
        return tmdbVoteAverage == null
                ? OptionalDouble.empty()
                : OptionalDouble.of(tmdbVoteAverage);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MovieDetails movieDetails)) {
            return false;
        }
        return movie.equals(movieDetails.movie);
    }

    @Override
    public int hashCode() {
        return movie.hashCode();
    }

    @Override
    public String toString() {
        return "MovieDetails{movie=" + movie + "}";
    }

    private static Integer validateRuntime(Integer runtimeMinutes) {
        if (runtimeMinutes != null && runtimeMinutes < 0) {
            throw new IllegalArgumentException("Runtime must not be negative");
        }
        return runtimeMinutes;
    }

    private static Double validateVoteAverage(Double voteAverage) {
        if (voteAverage == null) {
            return null;
        }
        if (!Double.isFinite(voteAverage)
                || voteAverage < MINIMUM_VOTE_AVERAGE
                || voteAverage > MAXIMUM_VOTE_AVERAGE) {
            throw new IllegalArgumentException("TMDB vote average must be from 0 to 10");
        }
        return voteAverage;
    }

    private static List<String> copyAndValidateGenres(List<String> genres) {
        Objects.requireNonNull(genres, "genres");
        List<String> normalizedGenres = new ArrayList<>(genres.size());
        for (String genre : genres) {
            Objects.requireNonNull(genre, "genre");
            String normalizedGenre = genre.trim();
            if (normalizedGenre.isEmpty()) {
                throw new IllegalArgumentException("Genre must not be blank");
            }
            normalizedGenres.add(normalizedGenre);
        }
        return List.copyOf(normalizedGenres);
    }

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
