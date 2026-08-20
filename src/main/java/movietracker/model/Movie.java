package movietracker.model;

import java.time.LocalDate;
import java.util.Objects;

public final class Movie {
    private final int tmdbId;
    private final String title;
    private final LocalDate releaseDate;
    private final String overview;
    private final String posterPath;
    private final Double externalRating;
    private WatchStatus watchStatus;

    public Movie(
            int tmdbId,
            String title,
            LocalDate releaseDate,
            String overview,
            String posterPath,
            Double externalRating,
            WatchStatus watchStatus) {
        if (tmdbId <= 0) {
            throw new IllegalArgumentException("TMDB movie ID must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Movie title must not be blank");
        }

        this.tmdbId = tmdbId;
        this.title = title;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.posterPath = posterPath;
        this.externalRating = externalRating;
        this.watchStatus = Objects.requireNonNull(watchStatus, "watchStatus must not be null");
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public Double getExternalRating() {
        return externalRating;
    }

    public WatchStatus getWatchStatus() {
        return watchStatus;
    }

    public void markAsWatched() {
        watchStatus = WatchStatus.WATCHED;
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
}
