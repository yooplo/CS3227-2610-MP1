package movietracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TrackedMovieTest {

    private static final Movie MOVIE = new Movie(550, "Fight Club", null, null);

    @Test
    void constructor_watchlistWithoutRating_createsTrackedMovie() {
        TrackedMovie trackedMovie = new TrackedMovie(MOVIE, WatchStatus.WATCHLIST, null);

        assertEquals(MOVIE, trackedMovie.getMovie());
        assertEquals(WatchStatus.WATCHLIST, trackedMovie.getStatus());
        assertTrue(trackedMovie.getPersonalRating().isEmpty());
    }

    @Test
    void constructor_watchedWithBoundaryRatings_acceptsRatings() {
        TrackedMovie minimum = new TrackedMovie(MOVIE, WatchStatus.WATCHED, 1);
        TrackedMovie maximum = new TrackedMovie(MOVIE, WatchStatus.WATCHED, 10);

        assertEquals(1, minimum.getPersonalRating().orElseThrow());
        assertEquals(10, maximum.getPersonalRating().orElseThrow());
    }

    @Test
    void constructor_invalidRating_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new TrackedMovie(MOVIE, WatchStatus.WATCHLIST, 5));
        assertThrows(IllegalArgumentException.class,
                () -> new TrackedMovie(MOVIE, WatchStatus.WATCHED, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new TrackedMovie(MOVIE, WatchStatus.WATCHED, 11));
    }

    @Test
    void constructor_missingRequiredValues_throwsException() {
        assertThrows(NullPointerException.class,
                () -> new TrackedMovie(null, WatchStatus.WATCHLIST, null));
        assertThrows(NullPointerException.class,
                () -> new TrackedMovie(MOVIE, null, null));
    }

    @Test
    void equality_sameTmdbIdentity_equalAcrossTrackingSnapshots() {
        Movie refreshedMovie = new Movie(550, "Updated title", null, null);
        TrackedMovie watchlist = new TrackedMovie(MOVIE, WatchStatus.WATCHLIST, null);
        TrackedMovie watched = new TrackedMovie(refreshedMovie, WatchStatus.WATCHED, 8);

        assertEquals(watchlist, watched);
        assertEquals(watchlist.hashCode(), watched.hashCode());
    }
}
