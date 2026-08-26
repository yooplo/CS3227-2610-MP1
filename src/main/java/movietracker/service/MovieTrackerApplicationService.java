package movietracker.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import movietracker.api.TmdbClient;
import movietracker.api.TmdbException;
import movietracker.model.Movie;
import movietracker.model.MovieDetails;
import movietracker.model.TrackedMovie;
import movietracker.model.WatchStatus;
import movietracker.storage.StorageException;

/**
 * Application-facing coordinator for remote movie lookup and local tracking.
 *
 * <p>This class is synchronous. A future JavaFX layer is responsible for invoking
 * remote operations away from the JavaFX Application Thread.</p>
 */
public final class MovieTrackerApplicationService {

    private final TmdbClient tmdbClient;
    private final MovieTrackerService movieTrackerService;

    /**
     * Creates an application service from its two use-case dependencies.
     *
     * @param tmdbClient remote movie lookup boundary
     * @param movieTrackerService local tracking use cases
     */
    public MovieTrackerApplicationService(
            TmdbClient tmdbClient, MovieTrackerService movieTrackerService) {
        this.tmdbClient = Objects.requireNonNull(tmdbClient, "tmdbClient");
        this.movieTrackerService = Objects.requireNonNull(
                movieTrackerService, "movieTrackerService");
    }

    public List<Movie> searchMovies(String query) throws TmdbException {
        return tmdbClient.searchMovies(query);
    }

    public MovieDetails getMovieDetails(int tmdbId) throws TmdbException {
        return tmdbClient.getMovieDetails(tmdbId);
    }

    public List<TrackedMovie> getTrackedMovies() {
        return movieTrackerService.getTrackedMovies();
    }

    public List<TrackedMovie> getWatchlist() {
        return movieTrackerService.getWatchlist();
    }

    public List<TrackedMovie> getWatched() {
        return movieTrackerService.getWatched();
    }

    public boolean isTracked(int tmdbId) {
        return movieTrackerService.isTracked(tmdbId);
    }

    public Optional<WatchStatus> getTrackingStatus(int tmdbId) {
        return movieTrackerService.getTrackingStatus(tmdbId);
    }

    public boolean addToWatchlist(Movie movie) throws StorageException {
        return movieTrackerService.addToWatchlist(movie);
    }

    public boolean markWatched(Movie movie) throws StorageException {
        return movieTrackerService.markWatched(movie);
    }

    public boolean removeTrackedMovie(int tmdbId) throws StorageException {
        return movieTrackerService.removeTrackedMovie(tmdbId);
    }

    public boolean setPersonalRating(int tmdbId, Integer personalRating)
            throws StorageException {
        return movieTrackerService.setPersonalRating(tmdbId, personalRating);
    }
}
