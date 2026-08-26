package movietracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import movietracker.api.TmdbClient;
import movietracker.model.TrackedMovie;
import movietracker.service.MovieTrackerApplicationService;
import movietracker.storage.Storage;
import movietracker.storage.StorageException;

class MovieTrackerAppTest {

    @Test
    void createApplicationService_emptyStorageStartsNormallyWithoutSaving()
            throws StorageException {
        StubStorage storage = new StubStorage(List.of());

        MovieTrackerApplicationService applicationService =
                MovieTrackerApp.createApplicationService(
                        storage, TmdbClient.fromEnvironment());

        assertTrue(applicationService.getTrackedMovies().isEmpty());
        assertEquals(1, storage.loadCalls);
        assertEquals(0, storage.saveCalls);
    }

    @Test
    void createApplicationService_loadFailurePropagatesOriginalFailureWithoutSaving() {
        StorageException failure = new StorageException("simulated corrupted storage");
        StubStorage storage = new StubStorage(failure);

        StorageException actual = assertThrows(
                StorageException.class,
                () -> MovieTrackerApp.createApplicationService(
                        storage, TmdbClient.fromEnvironment()));

        assertSame(failure, actual);
        assertEquals(1, storage.loadCalls);
        assertEquals(0, storage.saveCalls);
    }

    private static final class StubStorage implements Storage {

        private final List<TrackedMovie> loadedMovies;
        private final StorageException loadFailure;
        private int loadCalls;
        private int saveCalls;

        private StubStorage(List<TrackedMovie> loadedMovies) {
            this.loadedMovies = List.copyOf(loadedMovies);
            this.loadFailure = null;
        }

        private StubStorage(StorageException loadFailure) {
            this.loadedMovies = List.of();
            this.loadFailure = loadFailure;
        }

        @Override
        public List<TrackedMovie> load() throws StorageException {
            loadCalls++;
            if (loadFailure != null) {
                throw loadFailure;
            }
            return loadedMovies;
        }

        @Override
        public void save(Collection<TrackedMovie> movies) {
            saveCalls++;
        }
    }
}
