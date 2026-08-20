package movietracker.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import movietracker.application.MovieCollectionManager.MutationResult;
import movietracker.model.Movie;
import movietracker.model.MovieCollection;
import movietracker.model.WatchStatus;
import movietracker.storage.MovieStorage;

class MovieCollectionManagerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void successfulMutationsAreSaved() throws Exception {
        MovieStorage storage = storage();
        MovieCollectionManager manager = new MovieCollectionManager(
                new MovieCollection(), storage, true);

        assertEquals(MutationResult.SUCCESS, manager.add(movie(1)));
        assertTrue(storage.load().findByTmdbId(1).isPresent());

        assertEquals(MutationResult.SUCCESS, manager.markAsWatched(1));
        assertEquals(WatchStatus.WATCHED,
                storage.load().findByTmdbId(1).orElseThrow().getWatchStatus());

        assertEquals(MutationResult.SUCCESS, manager.remove(1));
        assertTrue(storage.load().getAllMovies().isEmpty());
    }

    @Test
    void noChangeDoesNotCreateStorageFile() {
        MovieStorage storage = storage();
        MovieCollectionManager manager = new MovieCollectionManager(
                new MovieCollection(), storage, true);

        assertEquals(MutationResult.NO_CHANGE, manager.remove(999));
        assertFalse(Files.exists(storage.getStoragePath()));
    }

    @Test
    void disabledPersistenceNeverOverwritesExistingCorruptedFile() throws Exception {
        MovieStorage storage = storage();
        Files.createDirectories(storage.getStoragePath().getParent());
        String corruptedData = "corrupted saved data";
        Files.writeString(storage.getStoragePath(), corruptedData);
        MovieCollectionManager manager = new MovieCollectionManager(
                new MovieCollection(), storage, false);

        assertEquals(MutationResult.PERSISTENCE_DISABLED, manager.add(movie(1)));

        assertTrue(manager.findByTmdbId(1).isPresent());
        assertEquals(corruptedData, Files.readString(storage.getStoragePath()));
    }

    @Test
    void saveFailureDoesNotUndoInMemoryDomainChange() throws Exception {
        Path regularFile = temporaryDirectory.resolve("not-a-directory");
        Files.writeString(regularFile, "occupied");
        MovieStorage storage = new MovieStorage(regularFile.resolve("movies.json"));
        MovieCollectionManager manager = new MovieCollectionManager(
                new MovieCollection(), storage, true);

        assertEquals(MutationResult.SAVE_FAILED, manager.add(movie(1)));
        assertTrue(manager.findByTmdbId(1).isPresent());
    }

    private MovieStorage storage() {
        return new MovieStorage(temporaryDirectory.resolve("data").resolve("movies.json"));
    }

    private static Movie movie(int id) {
        return new Movie(id, "Movie " + id, null, null, null, null, WatchStatus.WATCHLIST);
    }
}
