package movietracker.model;

import java.time.LocalDate;

public record MovieInfo(
        int tmdbId,
        String title,
        LocalDate releaseDate,
        String overview,
        String posterPath,
        Double externalRating) {

    public MovieInfo {
        if (tmdbId <= 0) {
            throw new IllegalArgumentException("TMDB movie ID must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Movie title must not be blank");
        }
    }
}
