package movietracker.ui;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import movietracker.model.MovieInfo;

final class MovieDetailsText {
    private static final DateTimeFormatter RELEASE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private MovieDetailsText() {
    }

    static String releaseDate(MovieInfo movie) {
        if (movie.releaseDate() == null) {
            return "Release date unavailable.";
        }
        return "Release date: " + RELEASE_DATE_FORMAT.format(movie.releaseDate());
    }

    static String rating(MovieInfo movie) {
        if (movie.externalRating() == null) {
            return "TMDB rating unavailable.";
        }
        return String.format(Locale.ROOT, "TMDB rating: %.1f/10", movie.externalRating());
    }

    static String overview(MovieInfo movie) {
        if (movie.overview() == null || movie.overview().isBlank()) {
            return "No overview is available for this movie.";
        }
        return movie.overview();
    }
}
