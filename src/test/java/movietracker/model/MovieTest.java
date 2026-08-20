package movietracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class MovieTest {
    @Test
    void moviesWithSameTmdbIdAreEqualDespiteDifferentMetadata() {
        Movie first = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        Movie second = movie(157336, "Different metadata", WatchStatus.WATCHED);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void moviesWithDifferentTmdbIdsAreNotEqual() {
        assertNotEquals(
                movie(157336, "Interstellar", WatchStatus.WATCHLIST),
                movie(27205, "Inception", WatchStatus.WATCHLIST));
    }

    @Test
    void changingStatusDoesNotBreakHashBasedIdentity() {
        Movie movie = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        Set<Movie> movies = new HashSet<>();
        movies.add(movie);

        movie.markAsWatched();

        assertEquals(WatchStatus.WATCHED, movie.getWatchStatus());
        assertTrue(movies.contains(movie));
    }

    @Test
    void constructorRejectsInvalidIdentityAndRequiredFields() {
        assertThrows(IllegalArgumentException.class,
                () -> movie(0, "Interstellar", WatchStatus.WATCHLIST));
        assertThrows(IllegalArgumentException.class,
                () -> movie(157336, "   ", WatchStatus.WATCHLIST));
        assertThrows(NullPointerException.class,
                () -> movie(157336, "Interstellar", null));
    }

    @Test
    void constructorRejectsInvalidExternalRatings() {
        assertThrows(IllegalArgumentException.class,
                () -> movieWithRating(-0.1));
        assertThrows(IllegalArgumentException.class,
                () -> movieWithRating(10.1));
        assertThrows(IllegalArgumentException.class,
                () -> movieWithRating(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> movieWithRating(Double.POSITIVE_INFINITY));
    }

    private static Movie movie(int tmdbId, String title, WatchStatus status) {
        return new Movie(
                tmdbId,
                title,
                LocalDate.of(2014, 11, 7),
                "Overview",
                "/poster.jpg",
                8.7,
                status);
    }

    private static Movie movieWithRating(double externalRating) {
        return new Movie(
                157336,
                "Interstellar",
                null,
                null,
                null,
                externalRating,
                WatchStatus.WATCHLIST);
    }
}
