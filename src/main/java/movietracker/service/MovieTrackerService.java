package movietracker.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import movietracker.model.Movie;
import movietracker.model.TrackedMovie;
import movietracker.model.WatchStatus;
import movietracker.storage.Storage;
import movietracker.storage.StorageException;

/**
 * Coordinates local movie-tracking rules and persistence.
 */
public final class MovieTrackerService {

    private final Storage storage;
    private List<TrackedMovie> trackedMovies;

    /**
     * Creates the service and loads its initial state from storage.
     *
     * @param storage persistence boundary
     * @throws StorageException if the stored state cannot be loaded or contains duplicate IDs
     */
    public MovieTrackerService(Storage storage) throws StorageException {
        this.storage = Objects.requireNonNull(storage, "storage");
        this.trackedMovies = validateAndCopyLoadedState(storage.load());
    }

    /**
     * Returns all tracked movies in deterministic insertion order.
     */
    public List<TrackedMovie> getTrackedMovies() {
        return trackedMovies;
    }

    /**
     * Returns Watchlist movies in their tracked order.
     */
    public List<TrackedMovie> getWatchlist() {
        return filterByStatus(WatchStatus.WATCHLIST);
    }

    /**
     * Returns Watched movies in their tracked order.
     */
    public List<TrackedMovie> getWatched() {
        return filterByStatus(WatchStatus.WATCHED);
    }

    /**
     * Checks whether a TMDB movie ID is already tracked.
     */
    public boolean isTracked(int tmdbId) {
        validateTmdbId(tmdbId);
        return findIndex(tmdbId) >= 0;
    }

    /**
     * Returns the current tracking status for a TMDB movie ID.
     */
    public Optional<WatchStatus> getTrackingStatus(int tmdbId) {
        validateTmdbId(tmdbId);
        int index = findIndex(tmdbId);
        return index < 0
                ? Optional.empty()
                : Optional.of(trackedMovies.get(index).getStatus());
    }

    /**
     * Adds an untracked movie to Watchlist.
     *
     * @return {@code true} when added; {@code false} if the ID was already tracked
     * @throws StorageException if persistence fails
     */
    public boolean addToWatchlist(Movie movie) throws StorageException {
        Objects.requireNonNull(movie, "movie");
        if (findIndex(movie.getTmdbId()) >= 0) {
            return false;
        }

        List<TrackedMovie> proposedState = new ArrayList<>(trackedMovies);
        proposedState.add(new TrackedMovie(movie, WatchStatus.WATCHLIST, null));
        persistAndReplace(proposedState);
        return true;
    }

    /**
     * Marks a movie as watched. An untracked movie is added directly as watched.
     *
     * @return {@code true} when state changed; {@code false} if already watched
     * @throws StorageException if persistence fails
     */
    public boolean markWatched(Movie movie) throws StorageException {
        Objects.requireNonNull(movie, "movie");
        int index = findIndex(movie.getTmdbId());
        if (index < 0) {
            List<TrackedMovie> proposedState = new ArrayList<>(trackedMovies);
            proposedState.add(new TrackedMovie(movie, WatchStatus.WATCHED, null));
            persistAndReplace(proposedState);
            return true;
        }

        TrackedMovie existing = trackedMovies.get(index);
        if (existing.getStatus() == WatchStatus.WATCHED) {
            return false;
        }

        List<TrackedMovie> proposedState = new ArrayList<>(trackedMovies);
        proposedState.set(index, new TrackedMovie(
                existing.getMovie(), WatchStatus.WATCHED, null));
        persistAndReplace(proposedState);
        return true;
    }

    /**
     * Removes a movie from local tracking.
     *
     * @return {@code true} when removed; {@code false} if it was not tracked
     * @throws StorageException if persistence fails
     */
    public boolean removeTrackedMovie(int tmdbId) throws StorageException {
        validateTmdbId(tmdbId);
        int index = findIndex(tmdbId);
        if (index < 0) {
            return false;
        }

        List<TrackedMovie> proposedState = new ArrayList<>(trackedMovies);
        proposedState.remove(index);
        persistAndReplace(proposedState);
        return true;
    }

    /**
     * Sets or clears a watched movie's personal rating.
     *
     * @param tmdbId tracked TMDB movie ID
     * @param personalRating rating from 1 to 10, or {@code null} to clear it
     * @return {@code true} when the rating changed; {@code false} for the same value
     * @throws IllegalStateException if the movie is untracked or still in Watchlist
     * @throws IllegalArgumentException if a non-null rating is outside 1 to 10
     * @throws StorageException if persistence fails
     */
    public boolean setPersonalRating(int tmdbId, Integer personalRating) throws StorageException {
        validateTmdbId(tmdbId);
        int index = findIndex(tmdbId);
        if (index < 0) {
            throw new IllegalStateException("Cannot rate an untracked movie");
        }

        TrackedMovie existing = trackedMovies.get(index);
        if (existing.getStatus() != WatchStatus.WATCHED) {
            throw new IllegalStateException("Only watched movies may have a personal rating");
        }
        if (ratingsEqual(existing.getPersonalRating(), personalRating)) {
            return false;
        }

        TrackedMovie updated = new TrackedMovie(
                existing.getMovie(), WatchStatus.WATCHED, personalRating);
        List<TrackedMovie> proposedState = new ArrayList<>(trackedMovies);
        proposedState.set(index, updated);
        persistAndReplace(proposedState);
        return true;
    }

    private List<TrackedMovie> filterByStatus(WatchStatus status) {
        return trackedMovies.stream()
                .filter(movie -> movie.getStatus() == status)
                .toList();
    }

    private int findIndex(int tmdbId) {
        for (int index = 0; index < trackedMovies.size(); index++) {
            if (trackedMovies.get(index).getMovie().getTmdbId() == tmdbId) {
                return index;
            }
        }
        return -1;
    }

    private void persistAndReplace(List<TrackedMovie> proposedState) throws StorageException {
        List<TrackedMovie> immutableProposedState = List.copyOf(proposedState);
        storage.save(immutableProposedState);
        trackedMovies = immutableProposedState;
    }

    private static List<TrackedMovie> validateAndCopyLoadedState(List<TrackedMovie> loadedMovies)
            throws StorageException {
        if (loadedMovies == null) {
            throw new StorageException("Storage returned no tracked-movie collection");
        }

        Set<Integer> tmdbIds = new HashSet<>();
        for (TrackedMovie trackedMovie : loadedMovies) {
            if (trackedMovie == null) {
                throw new StorageException("Stored tracked-movie entry is missing");
            }
            int tmdbId = trackedMovie.getMovie().getTmdbId();
            if (!tmdbIds.add(tmdbId)) {
                throw new StorageException("Stored state contains duplicate TMDB movie ID: " + tmdbId);
            }
        }
        return List.copyOf(loadedMovies);
    }

    private static boolean ratingsEqual(OptionalInt currentRating, Integer proposedRating) {
        return proposedRating == null
                ? currentRating.isEmpty()
                : currentRating.isPresent() && currentRating.getAsInt() == proposedRating;
    }

    private static void validateTmdbId(int tmdbId) {
        if (tmdbId <= 0) {
            throw new IllegalArgumentException("TMDB movie ID must be positive");
        }
    }
}
