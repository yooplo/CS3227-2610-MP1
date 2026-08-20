package movietracker.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import movietracker.model.MovieCollection;
import movietracker.model.WatchStatus;

class MovieStorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void saveEmptyCollectionCreatesMissingDirectoryAndFile() throws Exception {
        Path storagePath = temporaryDirectory.resolve("missing").resolve("movies.json");
        MovieStorage storage = new MovieStorage(storagePath);

        storage.save(new MovieCollection());

        assertTrue(Files.isRegularFile(storagePath));
        assertTrue(storage.load().getAllMovies().isEmpty());
    }

    @Test
    void loadReturnsEmptyCollectionWhenFileAndDirectoryAreMissing() throws Exception {
        Path storagePath = temporaryDirectory.resolve("missing").resolve("movies.json");

        MovieCollection loaded = new MovieStorage(storagePath).load();

        assertTrue(loaded.getAllMovies().isEmpty());
        assertFalse(Files.exists(storagePath.getParent()));
    }

    @Test
    void loadReturnsEmptyCollectionWhenFileIsMissing() throws Exception {
        Path storagePath = temporaryDirectory.resolve("data").resolve("movies.json");
        Files.createDirectories(storagePath.getParent());

        MovieCollection loaded = new MovieStorage(storagePath).load();

        assertTrue(loaded.getAllMovies().isEmpty());
    }

    @Test
    void roundTripPreservesMultipleMoviesAndStatuses() throws Exception {
        MovieCollection collection = new MovieCollection();
        collection.add(movie(157336, "Interstellar", WatchStatus.WATCHLIST));
        collection.add(movie(27205, "Inception", WatchStatus.WATCHED));
        MovieStorage storage = storage();

        storage.save(collection);
        MovieCollection loaded = storage.load();

        assertEquals(List.of(157336), loaded.getWatchlistMovies().stream()
                .map(Movie::getTmdbId).toList());
        assertEquals(List.of(27205), loaded.getWatchedMovies().stream()
                .map(Movie::getTmdbId).toList());
        assertMovieFieldsEqual(collection.getAllMovies().get(0), loaded.getAllMovies().get(0));
        assertMovieFieldsEqual(collection.getAllMovies().get(1), loaded.getAllMovies().get(1));
    }

    @Test
    void roundTripPreservesNullableOptionalMetadata() throws Exception {
        MovieCollection collection = new MovieCollection();
        collection.add(new Movie(123, "Unknown metadata", null, null, null, null,
                WatchStatus.WATCHLIST));
        MovieStorage storage = storage();

        storage.save(collection);
        Movie loaded = storage.load().findByTmdbId(123).orElseThrow();

        assertNull(loaded.getReleaseDate());
        assertNull(loaded.getOverview());
        assertNull(loaded.getPosterPath());
        assertNull(loaded.getExternalRating());
    }

    @Test
    void malformedJsonThrowsAndRemainsUnchanged() throws IOException {
        Path storagePath = temporaryDirectory.resolve("movies.json");
        String corruptedData = "{ definitely not valid JSON";
        Files.writeString(storagePath, corruptedData);

        assertThrows(StorageException.class, () -> new MovieStorage(storagePath).load());
        assertEquals(corruptedData, Files.readString(storagePath));
    }

    @Test
    void invalidStoredMovieThrows() throws IOException {
        Path storagePath = temporaryDirectory.resolve("movies.json");
        Files.writeString(storagePath, """
                {
                  "version": 1,
                  "movies": [{
                    "tmdbId": -1,
                    "title": "Invalid",
                    "releaseDate": null,
                    "overview": null,
                    "posterPath": null,
                    "externalRating": null,
                    "watchStatus": "WATCHLIST"
                  }]
                }
                """);

        assertThrows(StorageException.class, () -> new MovieStorage(storagePath).load());
    }

    @Test
    void duplicateStoredMovieIdsAreRejected() throws IOException {
        Path storagePath = temporaryDirectory.resolve("movies.json");
        Files.writeString(storagePath, """
                {
                  "version": 1,
                  "movies": [
                    {"tmdbId": 1, "title": "First", "watchStatus": "WATCHLIST"},
                    {"tmdbId": 1, "title": "Duplicate", "watchStatus": "WATCHED"}
                  ]
                }
                """);

        assertThrows(StorageException.class, () -> new MovieStorage(storagePath).load());
    }

    @Test
    void saveFailureIsReported() throws IOException {
        Path regularFile = temporaryDirectory.resolve("not-a-directory");
        Files.writeString(regularFile, "occupied");
        MovieStorage storage = new MovieStorage(regularFile.resolve("movies.json"));

        assertThrows(StorageException.class, () -> storage.save(new MovieCollection()));
    }

    private MovieStorage storage() {
        return new MovieStorage(temporaryDirectory.resolve("data").resolve("movies.json"));
    }

    private static Movie movie(int id, String title, WatchStatus status) {
        return new Movie(
                id,
                title,
                LocalDate.of(2014, 11, 7),
                "Overview for " + title,
                "/" + id + ".jpg",
                8.7,
                status);
    }

    private static void assertMovieFieldsEqual(Movie expected, Movie actual) {
        assertEquals(expected.getTmdbId(), actual.getTmdbId());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getReleaseDate(), actual.getReleaseDate());
        assertEquals(expected.getOverview(), actual.getOverview());
        assertEquals(expected.getPosterPath(), actual.getPosterPath());
        assertEquals(expected.getExternalRating(), actual.getExternalRating());
        assertEquals(expected.getWatchStatus(), actual.getWatchStatus());
    }
}
