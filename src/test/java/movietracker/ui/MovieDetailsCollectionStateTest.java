package movietracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import movietracker.model.Movie;
import movietracker.model.WatchStatus;

class MovieDetailsCollectionStateTest {
    @Test
    void absentMovieIsUnsaved() {
        assertEquals(MovieDetailsCollectionState.UNSAVED,
                MovieDetailsCollectionState.from(Optional.empty()));
    }

    @Test
    void watchlistMovieIsWatchlist() {
        assertEquals(MovieDetailsCollectionState.WATCHLIST,
                MovieDetailsCollectionState.from(Optional.of(movie(WatchStatus.WATCHLIST))));
    }

    @Test
    void watchedMovieIsWatched() {
        assertEquals(MovieDetailsCollectionState.WATCHED,
                MovieDetailsCollectionState.from(Optional.of(movie(WatchStatus.WATCHED))));
    }

    private static Movie movie(WatchStatus status) {
        return new Movie(1, "Movie", null, null, null, null, status);
    }
}
