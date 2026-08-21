package movietracker.ui;

import java.util.Optional;

import movietracker.model.Movie;
import movietracker.model.WatchStatus;

enum MovieDetailsCollectionState {
    UNSAVED,
    WATCHLIST,
    WATCHED;

    static MovieDetailsCollectionState from(Optional<Movie> savedMovie) {
        if (savedMovie.isEmpty()) {
            return UNSAVED;
        }
        return savedMovie.orElseThrow().getWatchStatus() == WatchStatus.WATCHED
                ? WATCHED
                : WATCHLIST;
    }
}
