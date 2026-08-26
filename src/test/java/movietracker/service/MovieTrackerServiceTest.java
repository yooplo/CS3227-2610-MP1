package movietracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import movietracker.model.Movie;
import movietracker.model.TrackedMovie;
import movietracker.model.WatchStatus;
import movietracker.storage.Storage;
import movietracker.storage.StorageException;

class MovieTrackerServiceTest {

    private static final Movie FIGHT_CLUB = new Movie(550, "Fight Club", null, null);
    private static final Movie PULP_FICTION = new Movie(680, "Pulp Fiction", null, null);

    @Test
    void constructor_emptyStorage_startsEmpty() throws StorageException {
        FakeStorage storage = new FakeStorage(List.of());

        MovieTrackerService service = new MovieTrackerService(storage);

        assertTrue(service.getTrackedMovies().isEmpty());
        assertEquals(1, storage.loadCalls);
        assertEquals(0, storage.saveCalls);
    }

    @Test
    void constructor_existingState_loadsInDeterministicOrder() throws StorageException {
        TrackedMovie watched = tracked(PULP_FICTION, WatchStatus.WATCHED, 8);
        TrackedMovie watchlist = tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null);
        FakeStorage storage = new FakeStorage(List.of(watched, watchlist));

        MovieTrackerService service = new MovieTrackerService(storage);

        assertEquals(List.of(watched, watchlist), service.getTrackedMovies());
    }

    @Test
    void constructor_duplicateLoadedIds_throwsStorageException() {
        FakeStorage storage = new FakeStorage(List.of(
                tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null),
                tracked(new Movie(550, "Updated", null, null), WatchStatus.WATCHED, null)));

        assertThrows(StorageException.class, () -> new MovieTrackerService(storage));
    }

    @Test
    void addToWatchlist_untrackedMovie_addsAndPersists() throws StorageException {
        FakeStorage storage = new FakeStorage(List.of());
        MovieTrackerService service = new MovieTrackerService(storage);

        assertTrue(service.addToWatchlist(FIGHT_CLUB));

        assertEquals(WatchStatus.WATCHLIST, service.getTrackedMovies().getFirst().getStatus());
        assertEquals(1, storage.saveCalls);
        assertEquals(service.getTrackedMovies(), storage.lastSavedState());
    }

    @Test
    void addToWatchlist_duplicateIdInEitherStatus_isNoOpWithoutSave() throws StorageException {
        for (WatchStatus status : WatchStatus.values()) {
            FakeStorage storage = new FakeStorage(List.of(tracked(FIGHT_CLUB, status, null)));
            MovieTrackerService service = new MovieTrackerService(storage);

            boolean changed = service.addToWatchlist(new Movie(550, "Updated", null, null));

            assertFalse(changed);
            assertEquals(0, storage.saveCalls);
            assertEquals(status, service.getTrackedMovies().getFirst().getStatus());
        }
    }

    @Test
    void markWatched_watchlistMovie_replacesStatusAndPersists() throws StorageException {
        FakeStorage storage = new FakeStorage(List.of(
                tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null)));
        MovieTrackerService service = new MovieTrackerService(storage);

        assertTrue(service.markWatched(FIGHT_CLUB));

        TrackedMovie watchedMovie = tracked(FIGHT_CLUB, WatchStatus.WATCHED, null);
        assertTrue(service.getWatchlist().isEmpty());
        assertEquals(List.of(watchedMovie), service.getWatched());
        assertEquals(List.of(watchedMovie), storage.lastSavedState());
        assertEquals(1, storage.saveCalls);
    }

    @Test
    void markWatched_untrackedMovie_addsWatchedAndPersists() throws StorageException {
        FakeStorage storage = new FakeStorage(List.of());
        MovieTrackerService service = new MovieTrackerService(storage);

        assertTrue(service.markWatched(FIGHT_CLUB));

        TrackedMovie watchedMovie = tracked(FIGHT_CLUB, WatchStatus.WATCHED, null);
        assertTrue(service.getWatchlist().isEmpty());
        assertEquals(List.of(watchedMovie), service.getWatched());
        assertEquals(List.of(watchedMovie), storage.lastSavedState());
        assertEquals(1, storage.saveCalls);
    }

    @Test
    void markWatched_alreadyWatched_isNoOpWithoutSave() throws StorageException {
        TrackedMovie watched = tracked(FIGHT_CLUB, WatchStatus.WATCHED, 8);
        FakeStorage storage = new FakeStorage(List.of(watched));
        MovieTrackerService service = new MovieTrackerService(storage);

        assertFalse(service.markWatched(FIGHT_CLUB));

        assertEquals(List.of(watched), service.getTrackedMovies());
        assertEquals(0, storage.saveCalls);
    }

    @Test
    void remove_trackedAndUntracked_behavesAndPersistsOnlyChange() throws StorageException {
        FakeStorage storage = new FakeStorage(List.of(
                tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null)));
        MovieTrackerService service = new MovieTrackerService(storage);

        assertFalse(service.removeTrackedMovie(680));
        assertEquals(0, storage.saveCalls);
        assertTrue(service.removeTrackedMovie(550));
        assertTrue(service.getTrackedMovies().isEmpty());
        assertEquals(1, storage.saveCalls);
    }

    @Test
    void statusFilters_returnOnlyMatchingMoviesInOrder() throws StorageException {
        TrackedMovie firstWatchlist = tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null);
        TrackedMovie watched = tracked(PULP_FICTION, WatchStatus.WATCHED, 7);
        TrackedMovie secondWatchlist = tracked(
                new Movie(13, "Forrest Gump", null, null), WatchStatus.WATCHLIST, null);
        MovieTrackerService service = new MovieTrackerService(
                new FakeStorage(List.of(firstWatchlist, watched, secondWatchlist)));

        assertEquals(List.of(firstWatchlist, secondWatchlist), service.getWatchlist());
        assertEquals(List.of(watched), service.getWatched());
        assertTrue(service.isTracked(550));
        assertFalse(service.isTracked(999));
        assertEquals(WatchStatus.WATCHLIST,
                service.getTrackingStatus(550).orElseThrow());
        assertEquals(WatchStatus.WATCHED,
                service.getTrackingStatus(680).orElseThrow());
        assertTrue(service.getTrackingStatus(999).isEmpty());
    }

    @Test
    void rating_watchedMovie_setsUpdatesAndClearsWithPersistence() throws StorageException {
        FakeStorage storage = new FakeStorage(List.of(
                tracked(FIGHT_CLUB, WatchStatus.WATCHED, null)));
        MovieTrackerService service = new MovieTrackerService(storage);

        assertTrue(service.setPersonalRating(550, 7));
        assertEquals(7, service.getWatched().getFirst().getPersonalRating().orElseThrow());
        assertTrue(service.setPersonalRating(550, 9));
        assertEquals(9, service.getWatched().getFirst().getPersonalRating().orElseThrow());
        assertTrue(service.setPersonalRating(550, null));
        assertTrue(service.getWatched().getFirst().getPersonalRating().isEmpty());
        assertEquals(3, storage.saveCalls);
    }

    @Test
    void rating_sameValue_isNoOpWithoutSave() throws StorageException {
        FakeStorage storage = new FakeStorage(List.of(
                tracked(FIGHT_CLUB, WatchStatus.WATCHED, 8)));
        MovieTrackerService service = new MovieTrackerService(storage);

        assertFalse(service.setPersonalRating(550, 8));

        assertEquals(0, storage.saveCalls);
    }

    @Test
    void rating_watchlistOrUntracked_rejectedWithoutSave() throws StorageException {
        FakeStorage storage = new FakeStorage(List.of(
                tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null)));
        MovieTrackerService service = new MovieTrackerService(storage);

        assertThrows(IllegalStateException.class, () -> service.setPersonalRating(550, 8));
        assertThrows(IllegalStateException.class, () -> service.setPersonalRating(680, 8));
        assertEquals(0, storage.saveCalls);
    }

    @Test
    void rating_invalidValue_usesDomainValidationWithoutSave() throws StorageException {
        FakeStorage storage = new FakeStorage(List.of(
                tracked(FIGHT_CLUB, WatchStatus.WATCHED, null)));
        MovieTrackerService service = new MovieTrackerService(storage);

        assertThrows(IllegalArgumentException.class, () -> service.setPersonalRating(550, 0));
        assertEquals(0, storage.saveCalls);
    }

    @Test
    void saveFailure_doesNotPublishProposedInMemoryState() throws StorageException {
        TrackedMovie existing = tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null);
        FakeStorage storage = new FakeStorage(List.of(existing));
        storage.failSaves = true;
        MovieTrackerService service = new MovieTrackerService(storage);

        assertThrows(StorageException.class, () -> service.markWatched(FIGHT_CLUB));

        assertEquals(List.of(existing), service.getTrackedMovies());
        assertEquals(List.of(existing), service.getWatchlist());
        assertTrue(service.getWatched().isEmpty());
        assertEquals(1, storage.saveCalls);
    }

    @Test
    void returnedCollections_cannotModifyServiceState() throws StorageException {
        MovieTrackerService service = new MovieTrackerService(new FakeStorage(List.of(
                tracked(FIGHT_CLUB, WatchStatus.WATCHLIST, null),
                tracked(PULP_FICTION, WatchStatus.WATCHED, null))));

        assertThrows(UnsupportedOperationException.class,
                () -> service.getTrackedMovies().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> service.getWatchlist().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> service.getWatched().clear());
        assertEquals(2, service.getTrackedMovies().size());
    }

    private static TrackedMovie tracked(Movie movie, WatchStatus status, Integer rating) {
        return new TrackedMovie(movie, status, rating);
    }

    private static final class FakeStorage implements Storage {

        private final List<TrackedMovie> initialState;
        private final List<List<TrackedMovie>> savedStates = new ArrayList<>();
        private int loadCalls;
        private int saveCalls;
        private boolean failSaves;

        private FakeStorage(List<TrackedMovie> initialState) {
            this.initialState = List.copyOf(initialState);
        }

        @Override
        public List<TrackedMovie> load() {
            loadCalls++;
            return initialState;
        }

        @Override
        public void save(Collection<TrackedMovie> movies) throws StorageException {
            saveCalls++;
            if (failSaves) {
                throw new StorageException("simulated save failure");
            }
            savedStates.add(List.copyOf(movies));
        }

        private List<TrackedMovie> lastSavedState() {
            return savedStates.getLast();
        }
    }
}
