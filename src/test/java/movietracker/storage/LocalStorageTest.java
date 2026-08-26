package movietracker.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import movietracker.model.Movie;
import movietracker.model.TrackedMovie;
import movietracker.model.WatchStatus;

class LocalStorageTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void load_missingFile_returnsEmptyWithoutCreatingData() throws StorageException {
        Path dataFile = temporaryDirectory.resolve("missing").resolve("movies.json");
        LocalStorage storage = new LocalStorage(dataFile);

        List<TrackedMovie> loadedMovies = storage.load();

        assertTrue(loadedMovies.isEmpty());
        assertFalse(Files.exists(dataFile));
        assertFalse(Files.exists(dataFile.getParent()));
    }

    @Test
    void saveAndLoad_completeMovie_roundTripsAllPersistedFields() throws StorageException {
        Path dataFile = temporaryDirectory.resolve("data").resolve("movies.json");
        LocalStorage storage = new LocalStorage(dataFile);
        TrackedMovie expected = new TrackedMovie(
                new Movie(550, "Fight Club", LocalDate.of(1999, 10, 15), "/poster.jpg"),
                WatchStatus.WATCHED,
                9);

        storage.save(List.of(expected));
        List<TrackedMovie> loadedMovies = storage.load();

        assertEquals(1, loadedMovies.size());
        assertTrackedMovieEquals(expected, loadedMovies.getFirst());
    }

    @Test
    void saveAndLoad_multipleStatusesAndOptionalValues_roundTrips()
            throws StorageException {
        Path dataFile = temporaryDirectory.resolve("movies.json");
        LocalStorage storage = new LocalStorage(dataFile);
        TrackedMovie watchlistMovie = new TrackedMovie(
                new Movie(550, "Fight Club", null, null), WatchStatus.WATCHLIST, null);
        TrackedMovie watchedMovie = new TrackedMovie(
                new Movie(680, "Pulp Fiction", LocalDate.of(1994, 9, 10), "/pulp.jpg"),
                WatchStatus.WATCHED,
                null);

        storage.save(List.of(watchlistMovie, watchedMovie));
        List<TrackedMovie> loadedMovies = storage.load();

        assertEquals(2, loadedMovies.size());
        assertTrackedMovieEquals(watchlistMovie, loadedMovies.get(0));
        assertTrackedMovieEquals(watchedMovie, loadedMovies.get(1));
    }

    @Test
    void save_missingParentDirectory_createsDirectoryAndFile() throws StorageException {
        Path dataFile = temporaryDirectory.resolve("nested").resolve("data").resolve("movies.json");
        LocalStorage storage = new LocalStorage(dataFile);

        storage.save(List.of());

        assertTrue(Files.isDirectory(dataFile.getParent()));
        assertTrue(Files.isRegularFile(dataFile));
    }

    @Test
    void load_malformedJson_throwsStorageExceptionWithoutChangingFile() throws IOException {
        Path dataFile = temporaryDirectory.resolve("movies.json");
        String malformedJson = "{ definitely not valid JSON";
        Files.writeString(dataFile, malformedJson);
        LocalStorage storage = new LocalStorage(dataFile);

        StorageException exception = assertThrows(StorageException.class, storage::load);

        assertTrue(exception.getMessage().contains("Could not load tracked movies"));
        assertEquals(malformedJson, Files.readString(dataFile));
    }

    @Test
    void load_emptyFile_throwsStorageExceptionWithoutChangingFile() throws IOException {
        Path dataFile = temporaryDirectory.resolve("movies.json");
        Files.writeString(dataFile, "");
        LocalStorage storage = new LocalStorage(dataFile);

        assertThrows(StorageException.class, storage::load);

        assertEquals("", Files.readString(dataFile));
    }

    @Test
    void load_unsupportedVersion_throwsStorageExceptionWithoutChangingFile() throws IOException {
        Path dataFile = temporaryDirectory.resolve("movies.json");
        String unsupportedData = """
                {
                  "version": 2,
                  "movies": []
                }
                """;
        Files.writeString(dataFile, unsupportedData);
        LocalStorage storage = new LocalStorage(dataFile);

        StorageException exception = assertThrows(StorageException.class, storage::load);

        assertTrue(exception.getMessage().contains("Unsupported storage version"));
        assertEquals(unsupportedData, Files.readString(dataFile));
    }

    @Test
    void load_dataPathIsDirectory_throwsStorageExceptionWithoutReplacingPath() throws IOException {
        Path dataFile = temporaryDirectory.resolve("movies.json");
        Files.createDirectory(dataFile);
        LocalStorage storage = new LocalStorage(dataFile);

        assertThrows(StorageException.class, storage::load);

        assertTrue(Files.isDirectory(dataFile));
    }

    @Test
    void load_invalidDomainData_throwsStorageException() throws IOException {
        Path dataFile = temporaryDirectory.resolve("movies.json");
        Files.writeString(dataFile, """
                {
                  "version": 1,
                  "movies": [
                    {
                      "tmdbId": 0,
                      "title": "Invalid",
                      "releaseDate": null,
                      "posterPath": null,
                      "status": "WATCHLIST",
                      "personalRating": null
                    }
                  ]
                }
                """);
        LocalStorage storage = new LocalStorage(dataFile);

        assertThrows(StorageException.class, storage::load);
    }

    private static void assertTrackedMovieEquals(TrackedMovie expected, TrackedMovie actual) {
        assertEquals(expected.getMovie().getTmdbId(), actual.getMovie().getTmdbId());
        assertEquals(expected.getMovie().getTitle(), actual.getMovie().getTitle());
        assertEquals(expected.getMovie().getReleaseDate(), actual.getMovie().getReleaseDate());
        assertEquals(expected.getMovie().getPosterPath(), actual.getMovie().getPosterPath());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getPersonalRating(), actual.getPersonalRating());
    }
}
