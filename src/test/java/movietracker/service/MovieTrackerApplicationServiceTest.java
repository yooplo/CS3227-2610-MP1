package movietracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import movietracker.api.HttpTransport;
import movietracker.api.TmdbClient;
import movietracker.api.TmdbConfig;
import movietracker.api.TmdbErrorCategory;
import movietracker.api.TmdbException;
import movietracker.model.Movie;
import movietracker.model.MovieDetails;
import movietracker.model.TrackedMovie;
import movietracker.model.WatchStatus;
import movietracker.storage.Storage;
import movietracker.storage.StorageException;

class MovieTrackerApplicationServiceTest {

    private static final Movie FIGHT_CLUB = new Movie(
            550, "Fight Club", LocalDate.of(1999, 10, 15), "/fight-club.jpg");
    private static final Movie PULP_FICTION = new Movie(
            680, "Pulp Fiction", LocalDate.of(1994, 9, 10), "/pulp-fiction.jpg");

    @Test
    void searchMovies_delegatesToTmdbClientAndReturnsMappedMovies() throws Exception {
        StubTransport transport = new StubTransport(200, """
                {"results":[{"id":550,"title":"Fight Club",
                "release_date":"1999-10-15","poster_path":"/fight-club.jpg"}]}
                """);
        MovieTrackerApplicationService application = application(transport, new FakeStorage());

        List<Movie> results = application.searchMovies("Fight Club");

        assertEquals(List.of(FIGHT_CLUB), results);
        assertTrue(transport.lastRequest.uri().getPath().endsWith("/search/movie"));
        assertTrue(transport.lastRequest.uri().getRawQuery().contains("query=Fight%20Club"));
    }

    @Test
    void getMovieDetails_delegatesToTmdbClientAndReturnsMappedDetails() throws Exception {
        StubTransport transport = new StubTransport(200, """
                {"id":550,"title":"Fight Club","overview":"An insomniac meets a soap maker.",
                "release_date":"1999-10-15","runtime":139,
                "genres":[{"id":18,"name":"Drama"}],
                "poster_path":"/fight-club.jpg","backdrop_path":"/backdrop.jpg",
                "vote_average":8.4}
                """);
        MovieTrackerApplicationService application = application(transport, new FakeStorage());

        MovieDetails details = application.getMovieDetails(550);

        assertEquals(FIGHT_CLUB, details.getMovie());
        assertEquals(139, details.getRuntimeMinutes().orElseThrow());
        assertEquals(List.of("Drama"), details.getGenres());
        assertTrue(transport.lastRequest.uri().getPath().endsWith("/movie/550"));
    }

    @Test
    void trackingQueries_delegateToLocalService() throws Exception {
        TrackedMovie watchlist = tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null);
        TrackedMovie watched = tracked(PULP_FICTION, WatchStatus.WATCHED, 8);
        MovieTrackerApplicationService application = application(
                new StubTransport(200, "{}"), new FakeStorage(watchlist, watched));

        assertEquals(List.of(watchlist, watched), application.getTrackedMovies());
        assertEquals(List.of(watchlist), application.getWatchlist());
        assertEquals(List.of(watched), application.getWatched());
        assertTrue(application.isTracked(550));
        assertFalse(application.isTracked(999));
        assertEquals(WatchStatus.WATCHLIST,
                application.getTrackingStatus(550).orElseThrow());
        assertEquals(WatchStatus.WATCHED,
                application.getTrackingStatus(680).orElseThrow());
        assertTrue(application.getTrackingStatus(999).isEmpty());
    }

    @Test
    void trackingMutations_delegateToLocalServiceAndPersistence() throws Exception {
        FakeStorage storage = new FakeStorage();
        MovieTrackerApplicationService application = application(
                new StubTransport(200, "{}"), storage);

        assertTrue(application.addToWatchlist(FIGHT_CLUB));
        assertTrue(application.markWatched(FIGHT_CLUB));
        assertTrue(application.setPersonalRating(550, 9));
        assertEquals(9, application.getWatched().getFirst().getPersonalRating().orElseThrow());
        assertTrue(application.removeTrackedMovie(550));

        assertTrue(application.getTrackedMovies().isEmpty());
        assertEquals(4, storage.savedStates.size());
    }

    @Test
    void duplicateAdd_preservesMovieTrackerServiceBusinessRule() throws Exception {
        FakeStorage storage = new FakeStorage();
        MovieTrackerApplicationService application = application(
                new StubTransport(200, "{}"), storage);

        assertTrue(application.addToWatchlist(FIGHT_CLUB));
        assertFalse(application.addToWatchlist(
                new Movie(550, "Updated title", null, null)));

        assertEquals(List.of(tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null)),
                application.getWatchlist());
        assertEquals(1, storage.savedStates.size());
    }

    @Test
    void tmdbFailure_propagatesStructuredException() throws Exception {
        MovieTrackerApplicationService application = application(
                new StubTransport(503, "response body must not escape"), new FakeStorage());

        TmdbException exception = assertThrows(
                TmdbException.class, () -> application.searchMovies("Fight Club"));

        assertEquals(TmdbErrorCategory.HTTP_ERROR, exception.getCategory());
        assertEquals(503, exception.getStatusCode().orElseThrow());
        assertFalse(exception.getMessage().contains("response body must not escape"));
    }

    @Test
    void storageFailure_propagatesAndLocalStateRemainsUnchanged() throws Exception {
        FakeStorage storage = new FakeStorage();
        StorageException failure = new StorageException("simulated save failure");
        storage.saveFailure = failure;
        MovieTrackerApplicationService application = application(
                new StubTransport(200, "{}"), storage);

        StorageException actual = assertThrows(
                StorageException.class, () -> application.addToWatchlist(FIGHT_CLUB));

        assertSame(failure, actual);
        assertTrue(application.getTrackedMovies().isEmpty());
        assertTrue(application.getTrackingStatus(550).isEmpty());
    }

    @Test
    void removeStorageFailure_propagatesAndWatchlistRemainsUnchanged() throws Exception {
        TrackedMovie watchlistMovie = tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null);
        FakeStorage storage = new FakeStorage(watchlistMovie);
        StorageException failure = new StorageException("simulated remove failure");
        storage.saveFailure = failure;
        MovieTrackerApplicationService application = application(
                new StubTransport(200, "{}"), storage);

        StorageException actual = assertThrows(
                StorageException.class, () -> application.removeTrackedMovie(550));

        assertSame(failure, actual);
        assertEquals(List.of(watchlistMovie), application.getWatchlist());
        assertEquals(WatchStatus.WATCHLIST,
                application.getTrackingStatus(550).orElseThrow());
    }

    @Test
    void markWatchedStorageFailure_propagatesAndWatchlistRemainsUnchanged() throws Exception {
        TrackedMovie watchlistMovie = tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null);
        FakeStorage storage = new FakeStorage(watchlistMovie);
        StorageException failure = new StorageException("simulated mark-watched failure");
        storage.saveFailure = failure;
        MovieTrackerApplicationService application = application(
                new StubTransport(200, "{}"), storage);

        StorageException actual = assertThrows(
                StorageException.class, () -> application.markWatched(FIGHT_CLUB));

        assertSame(failure, actual);
        assertEquals(List.of(watchlistMovie), application.getWatchlist());
        assertTrue(application.getWatched().isEmpty());
        assertEquals(WatchStatus.WATCHLIST,
                application.getTrackingStatus(550).orElseThrow());
    }

    private static MovieTrackerApplicationService application(
            HttpTransport transport, FakeStorage storage) throws Exception {
        TmdbClient tmdbClient = new TmdbClient(
                new TmdbConfig("test-read-access-token"), transport);
        return new MovieTrackerApplicationService(
                tmdbClient, new MovieTrackerService(storage));
    }

    private static TrackedMovie tracked(Movie movie, WatchStatus status, Integer rating) {
        return new TrackedMovie(movie, status, rating);
    }

    private static final class StubTransport implements HttpTransport {

        private final HttpResult response;
        private HttpRequest lastRequest;

        private StubTransport(int statusCode, String responseBody) {
            response = new HttpResult(statusCode, responseBody);
        }

        @Override
        public HttpResult send(HttpRequest request) {
            lastRequest = request;
            return response;
        }
    }

    private static final class FakeStorage implements Storage {

        private final List<TrackedMovie> initialState;
        private final List<List<TrackedMovie>> savedStates = new ArrayList<>();
        private StorageException saveFailure;

        private FakeStorage(TrackedMovie... initialState) {
            this.initialState = List.of(initialState);
        }

        @Override
        public List<TrackedMovie> load() {
            return initialState;
        }

        @Override
        public void save(Collection<TrackedMovie> movies) throws StorageException {
            if (saveFailure != null) {
                throw saveFailure;
            }
            savedStates.add(List.copyOf(movies));
        }
    }
}
