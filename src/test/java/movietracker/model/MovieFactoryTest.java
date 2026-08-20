package movietracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class MovieFactoryTest {
    @Test
    void convertsAllMovieInfoFieldsAndAssignsWatchlistStatus() {
        MovieInfo movieInfo = new MovieInfo(
                157336,
                "Interstellar",
                LocalDate.of(2014, 11, 5),
                "Explorers travel through a wormhole.",
                "/poster.jpg",
                8.5);

        Movie movie = MovieFactory.fromMovieInfo(movieInfo);

        assertEquals(movieInfo.tmdbId(), movie.getTmdbId());
        assertEquals(movieInfo.title(), movie.getTitle());
        assertEquals(movieInfo.releaseDate(), movie.getReleaseDate());
        assertEquals(movieInfo.overview(), movie.getOverview());
        assertEquals(movieInfo.posterPath(), movie.getPosterPath());
        assertEquals(movieInfo.externalRating(), movie.getExternalRating());
        assertEquals(WatchStatus.WATCHLIST, movie.getWatchStatus());
    }

    @Test
    void preservesMissingOptionalMetadata() {
        Movie movie = MovieFactory.fromMovieInfo(
                new MovieInfo(157336, "Interstellar", null, null, null, null));

        assertNull(movie.getReleaseDate());
        assertNull(movie.getOverview());
        assertNull(movie.getPosterPath());
        assertNull(movie.getExternalRating());
    }

    @Test
    void rejectsNullMovieInfo() {
        assertThrows(NullPointerException.class, () -> MovieFactory.fromMovieInfo(null));
    }
}
