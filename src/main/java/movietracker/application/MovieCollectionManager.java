package movietracker.application;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import movietracker.model.Movie;
import movietracker.model.MovieCollection;
import movietracker.storage.MovieStorage;
import movietracker.storage.StorageException;

public final class MovieCollectionManager {
    public enum MutationResult {
        SUCCESS,
        NO_CHANGE,
        SAVE_FAILED,
        PERSISTENCE_DISABLED
    }

    private final MovieCollection movieCollection;
    private final MovieStorage movieStorage;
    private final boolean persistenceEnabled;

    public MovieCollectionManager(
            MovieCollection movieCollection,
            MovieStorage movieStorage,
            boolean persistenceEnabled) {
        this.movieCollection = Objects.requireNonNull(movieCollection);
        this.movieStorage = Objects.requireNonNull(movieStorage);
        this.persistenceEnabled = persistenceEnabled;
    }

    public MutationResult add(Movie movie) {
        if (!movieCollection.add(movie)) {
            return MutationResult.NO_CHANGE;
        }
        return saveAfterChange();
    }

    public MutationResult remove(int tmdbId) {
        if (!movieCollection.remove(tmdbId)) {
            return MutationResult.NO_CHANGE;
        }
        return saveAfterChange();
    }

    public MutationResult markAsWatched(int tmdbId) {
        if (!movieCollection.markAsWatched(tmdbId)) {
            return MutationResult.NO_CHANGE;
        }
        return saveAfterChange();
    }

    public Optional<Movie> findByTmdbId(int tmdbId) {
        return movieCollection.findByTmdbId(tmdbId);
    }

    public List<Movie> getWatchlistMovies() {
        return movieCollection.getWatchlistMovies();
    }

    public List<Movie> getWatchedMovies() {
        return movieCollection.getWatchedMovies();
    }

    private MutationResult saveAfterChange() {
        if (!persistenceEnabled) {
            return MutationResult.PERSISTENCE_DISABLED;
        }

        try {
            movieStorage.save(movieCollection);
            return MutationResult.SUCCESS;
        } catch (StorageException exception) {
            return MutationResult.SAVE_FAILED;
        }
    }
}
