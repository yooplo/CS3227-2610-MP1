package movietracker.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable movie snapshot identified by its TMDB movie ID.
 */
public final class Movie {

    private final int tmdbId;
    private final String title;
    private final LocalDate releaseDate;
    private final String posterPath;

    /**
     * Creates a movie snapshot.
     *
     * @param tmdbId positive TMDB movie ID
     * @param title non-blank movie title
     * @param releaseDate release date, or {@code null} when unavailable
     * @param posterPath TMDB poster path, or {@code null} when unavailable
     */
    public Movie(int tmdbId, String title, LocalDate releaseDate, String posterPath) {
        if (tmdbId <= 0) {
            throw new IllegalArgumentException("TMDB movie ID must be positive");
        }

        Objects.requireNonNull(title, "title");
        String normalizedTitle = title.trim();
        if (normalizedTitle.isEmpty()) {
            throw new IllegalArgumentException("Movie title must not be blank");
        }

        this.tmdbId = tmdbId;
        this.title = normalizedTitle;
        this.releaseDate = releaseDate;
        this.posterPath = normalizeOptionalText(posterPath);
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public String getTitle() {
        return title;
    }

    public Optional<LocalDate> getReleaseDate() {
        return Optional.ofNullable(releaseDate);
    }

    public Optional<String> getPosterPath() {
        return Optional.ofNullable(posterPath);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Movie movie)) {
            return false;
        }
        return tmdbId == movie.tmdbId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(tmdbId);
    }

    @Override
    public String toString() {
        return "Movie{tmdbId=" + tmdbId + ", title='" + title + "'}";
    }

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
