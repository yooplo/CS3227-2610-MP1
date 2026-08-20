package movietracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MovieCollectionTest {
    private MovieCollection collection;

    @BeforeEach
    void setUp() {
        collection = new MovieCollection();
    }

    @Test
    void addStoresMovie() {
        Movie movie = movie(157336, "Interstellar", WatchStatus.WATCHLIST);

        assertTrue(collection.add(movie));
        assertEquals(List.of(movie), collection.getAllMovies());
    }

    @Test
    void addRejectsDuplicateTmdbIdAndKeepsOriginalMovie() {
        Movie original = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        Movie duplicate = movie(157336, "Changed title", WatchStatus.WATCHED);

        assertTrue(collection.add(original));
        assertFalse(collection.add(duplicate));
        assertEquals(List.of(original), collection.getAllMovies());
        assertEquals(WatchStatus.WATCHLIST,
                collection.findByTmdbId(157336).orElseThrow().getWatchStatus());
    }

    @Test
    void addRejectsNull() {
        assertThrows(NullPointerException.class, () -> collection.add(null));
    }

    @Test
    void removeDeletesOnlyMovieWithRequestedId() {
        Movie removedMovie = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        Movie retainedMovie = movie(27205, "Inception", WatchStatus.WATCHLIST);
        collection.add(removedMovie);
        collection.add(retainedMovie);

        assertTrue(collection.remove(157336));
        assertFalse(collection.findByTmdbId(157336).isPresent());
        assertEquals(List.of(retainedMovie), collection.getAllMovies());
    }

    @Test
    void removeReturnsFalseWhenMovieDoesNotExist() {
        assertFalse(collection.remove(999999));
    }

    @Test
    void findByTmdbIdReturnsMatchingMovieOrEmptyResult() {
        Movie movie = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        collection.add(movie);

        assertEquals(movie, collection.findByTmdbId(157336).orElseThrow());
        assertTrue(collection.findByTmdbId(999999).isEmpty());
    }

    @Test
    void markAsWatchedChangesStatusOfMatchingMovie() {
        Movie movie = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        collection.add(movie);

        assertTrue(collection.markAsWatched(157336));
        assertEquals(WatchStatus.WATCHED, movie.getWatchStatus());
        assertFalse(collection.markAsWatched(999999));
    }

    @Test
    void markAsWatchedReportsNoChangeForAlreadyWatchedMovie() {
        Movie movie = movie(157336, "Interstellar", WatchStatus.WATCHED);
        collection.add(movie);

        assertFalse(collection.markAsWatched(157336));
        assertEquals(WatchStatus.WATCHED, movie.getWatchStatus());
    }

    @Test
    void markAsWatchedMovesMovieBetweenStatusLists() {
        Movie movie = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        collection.add(movie);

        collection.markAsWatched(157336);

        assertTrue(collection.getWatchlistMovies().isEmpty());
        assertEquals(List.of(movie), collection.getWatchedMovies());
    }

    @Test
    void duplicateAddDoesNotChangeWatchedMovieBackToWatchlist() {
        Movie watchedMovie = movie(157336, "Interstellar", WatchStatus.WATCHED);
        collection.add(watchedMovie);

        assertFalse(collection.add(movie(157336, "Changed metadata", WatchStatus.WATCHLIST)));

        assertTrue(collection.getWatchlistMovies().isEmpty());
        assertEquals(List.of(watchedMovie), collection.getWatchedMovies());
    }

    @Test
    void statusListsContainOnlyMoviesWithRequestedStatus() {
        Movie watchlistMovie = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        Movie watchedMovie = movie(27205, "Inception", WatchStatus.WATCHED);
        collection.add(watchlistMovie);
        collection.add(watchedMovie);

        assertEquals(List.of(watchlistMovie), collection.getWatchlistMovies());
        assertEquals(List.of(watchedMovie), collection.getWatchedMovies());
    }

    @Test
    void titleSearchIsCaseInsensitiveAndMatchesSubstrings() {
        Movie interstellar = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        Movie inception = movie(27205, "Inception", WatchStatus.WATCHLIST);
        collection.add(interstellar);
        collection.add(inception);

        assertEquals(List.of(interstellar), collection.searchByTitle("STELLAR"));
        assertEquals(List.of(inception), collection.searchByTitle("  inCEP  "));
        assertTrue(collection.searchByTitle("unknown").isEmpty());
    }

    @Test
    void blankTitleSearchReturnsAllMovies() {
        Movie first = movie(157336, "Interstellar", WatchStatus.WATCHLIST);
        Movie second = movie(27205, "Inception", WatchStatus.WATCHED);
        collection.add(first);
        collection.add(second);

        assertEquals(List.of(first, second), collection.searchByTitle("   "));
    }

    @Test
    void titleSearchRejectsNull() {
        assertThrows(NullPointerException.class, () -> collection.searchByTitle(null));
    }

    @Test
    void returnedMovieListsCannotModifyCollectionStructure() {
        collection.add(movie(157336, "Interstellar", WatchStatus.WATCHLIST));

        List<Movie> movies = collection.getAllMovies();

        assertThrows(UnsupportedOperationException.class, movies::clear);
        assertEquals(1, collection.getAllMovies().size());
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
}
