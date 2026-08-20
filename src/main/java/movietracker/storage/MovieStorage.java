package movietracker.storage;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import movietracker.model.Movie;
import movietracker.model.MovieCollection;
import movietracker.model.WatchStatus;

public final class MovieStorage {
    private static final int FORMAT_VERSION = 1;

    private final Path storagePath;
    private final ObjectMapper objectMapper;

    public MovieStorage(Path storagePath) {
        this.storagePath = Objects.requireNonNull(storagePath, "storagePath must not be null");
        this.objectMapper = new ObjectMapper();
    }

    public MovieCollection load() throws StorageException {
        if (Files.notExists(storagePath)) {
            return new MovieCollection();
        }

        try {
            StoredCollection storedCollection = objectMapper.readValue(
                    storagePath.toFile(), StoredCollection.class);
            return toMovieCollection(storedCollection);
        } catch (IOException | IllegalArgumentException exception) {
            throw new StorageException("Saved movie data could not be loaded", exception);
        }
    }

    public void save(MovieCollection movieCollection) throws StorageException {
        Objects.requireNonNull(movieCollection, "movieCollection must not be null");

        Path directory = storagePath.getParent();
        if (directory == null) {
            directory = Path.of(".");
        }

        Path temporaryFile = null;
        try {
            Files.createDirectories(directory);
            temporaryFile = Files.createTempFile(directory, "movies-", ".tmp");
            StoredCollection storedCollection = StoredCollection.from(movieCollection);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporaryFile.toFile(), storedCollection);
            replaceStorageFile(temporaryFile);
        } catch (IOException exception) {
            deleteTemporaryFile(temporaryFile);
            throw new StorageException("Saved movie data could not be written", exception);
        }
    }

    public Path getStoragePath() {
        return storagePath;
    }

    private MovieCollection toMovieCollection(StoredCollection storedCollection) {
        if (storedCollection == null || storedCollection.version() != FORMAT_VERSION
                || storedCollection.movies() == null) {
            throw new IllegalArgumentException("Invalid movie storage document");
        }

        MovieCollection movieCollection = new MovieCollection();
        for (StoredMovie storedMovie : storedCollection.movies()) {
            if (storedMovie == null || !movieCollection.add(storedMovie.toMovie())) {
                throw new IllegalArgumentException("Invalid or duplicate stored movie");
            }
        }
        return movieCollection;
    }

    private void replaceStorageFile(Path temporaryFile) throws IOException {
        try {
            Files.move(
                    temporaryFile,
                    storagePath,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, storagePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteTemporaryFile(Path temporaryFile) {
        if (temporaryFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ignored) {
            // The original storage failure remains the useful error to report.
        }
    }

    private record StoredCollection(int version, List<StoredMovie> movies) {
        private static StoredCollection from(MovieCollection movieCollection) {
            List<StoredMovie> movies = movieCollection.getAllMovies().stream()
                    .map(StoredMovie::from)
                    .toList();
            return new StoredCollection(FORMAT_VERSION, movies);
        }
    }

    private record StoredMovie(
            int tmdbId,
            String title,
            String releaseDate,
            String overview,
            String posterPath,
            Double externalRating,
            WatchStatus watchStatus) {
        private static StoredMovie from(Movie movie) {
            return new StoredMovie(
                    movie.getTmdbId(),
                    movie.getTitle(),
                    movie.getReleaseDate() == null ? null : movie.getReleaseDate().toString(),
                    movie.getOverview(),
                    movie.getPosterPath(),
                    movie.getExternalRating(),
                    movie.getWatchStatus());
        }

        private Movie toMovie() {
            if (externalRating != null
                    && (!Double.isFinite(externalRating) || externalRating < 0 || externalRating > 10)) {
                throw new IllegalArgumentException("Invalid external rating");
            }
            LocalDate parsedReleaseDate = releaseDate == null || releaseDate.isBlank()
                    ? null
                    : LocalDate.parse(releaseDate);
            return new Movie(
                    tmdbId,
                    title,
                    parsedReleaseDate,
                    overview,
                    posterPath,
                    externalRating,
                    watchStatus);
        }
    }
}
