package movietracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import movietracker.model.MovieInfo;

class MovieDetailsTextTest {
    @Test
    void formatsAvailableMetadata() {
        MovieInfo movie = new MovieInfo(
                157336,
                "Interstellar",
                LocalDate.of(2014, 11, 5),
                "Explorers travel through a wormhole.",
                null,
                8.5);

        assertEquals("Release date: 2014-11-05", MovieDetailsText.releaseDate(movie));
        assertEquals("TMDB rating: 8.5/10", MovieDetailsText.rating(movie));
        assertEquals("Explorers travel through a wormhole.", MovieDetailsText.overview(movie));
    }

    @Test
    void suppliesFallbacksForMissingMetadata() {
        MovieInfo movie = new MovieInfo(157336, "Interstellar", null, null, null, null);

        assertEquals("Release date unavailable.", MovieDetailsText.releaseDate(movie));
        assertEquals("TMDB rating unavailable.", MovieDetailsText.rating(movie));
        assertEquals("No overview is available for this movie.", MovieDetailsText.overview(movie));
    }
}
