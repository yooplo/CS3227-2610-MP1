package movietracker.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MovieCollection {
    private final Map<Integer, Movie> moviesByTmdbId = new LinkedHashMap<>();

    public boolean add(Movie movie) {
        Objects.requireNonNull(movie, "movie must not be null");
        return moviesByTmdbId.putIfAbsent(movie.getTmdbId(), movie) == null;
    }

    public boolean remove(int tmdbId) {
        return moviesByTmdbId.remove(tmdbId) != null;
    }

    public Optional<Movie> findByTmdbId(int tmdbId) {
        return Optional.ofNullable(moviesByTmdbId.get(tmdbId));
    }

    public List<Movie> getAllMovies() {
        return List.copyOf(moviesByTmdbId.values());
    }

    public List<Movie> getWatchlistMovies() {
        return getMoviesWithStatus(WatchStatus.WATCHLIST);
    }

    public List<Movie> getWatchedMovies() {
        return getMoviesWithStatus(WatchStatus.WATCHED);
    }

    public boolean markAsWatched(int tmdbId) {
        Movie movie = moviesByTmdbId.get(tmdbId);
        if (movie == null || movie.getWatchStatus() == WatchStatus.WATCHED) {
            return false;
        }

        movie.markAsWatched();
        return true;
    }

    public List<Movie> searchByTitle(String query) {
        Objects.requireNonNull(query, "query must not be null");
        String normalizedQuery = query.strip().toLowerCase(Locale.ROOT);

        return moviesByTmdbId.values().stream()
                .filter(movie -> movie.getTitle().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .toList();
    }

    private List<Movie> getMoviesWithStatus(WatchStatus status) {
        return moviesByTmdbId.values().stream()
                .filter(movie -> movie.getWatchStatus() == status)
                .toList();
    }
}
