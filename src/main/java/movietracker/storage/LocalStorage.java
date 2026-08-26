package movietracker.storage;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import movietracker.model.Movie;
import movietracker.model.TrackedMovie;
import movietracker.model.WatchStatus;

/**
 * Stores the complete tracked-movie state in a versioned JSON file.
 */
public final class LocalStorage implements Storage {

    public static final Path DEFAULT_DATA_FILE = Path.of("data", "movies.json");

    private static final int CURRENT_FORMAT_VERSION = 1;

    private final Path dataFile;
    private final ObjectMapper objectMapper;

    /**
     * Creates storage using {@code data/movies.json} relative to the working directory.
     */
    public LocalStorage() {
        this(DEFAULT_DATA_FILE);
    }

    /**
     * Creates storage for a caller-supplied path.
     *
     * @param dataFile JSON data-file path
     */
    public LocalStorage(Path dataFile) {
        this.dataFile = Objects.requireNonNull(dataFile, "dataFile").toAbsolutePath().normalize();
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<TrackedMovie> load() throws StorageException {
        if (Files.notExists(dataFile)) {
            return List.of();
        }

        try {
            StorageData storageData = objectMapper.readValue(dataFile.toFile(), StorageData.class);
            return toDomainMovies(storageData);
        } catch (IOException | RuntimeException exception) {
            throw new StorageException("Could not load tracked movies from " + dataFile, exception);
        }
    }

    @Override
    public void save(Collection<TrackedMovie> movies) throws StorageException {
        Objects.requireNonNull(movies, "movies");
        Path parentDirectory = dataFile.getParent();
        Path temporaryFile = null;

        try {
            Files.createDirectories(parentDirectory);
            temporaryFile = Files.createTempFile(
                    parentDirectory, dataFile.getFileName().toString(), ".tmp");
            objectMapper.writeValue(temporaryFile.toFile(), toStorageData(movies));
            replaceDataFile(temporaryFile);
            temporaryFile = null;
        } catch (IOException | RuntimeException exception) {
            throw new StorageException("Could not save tracked movies to " + dataFile, exception);
        } finally {
            deleteTemporaryFile(temporaryFile);
        }
    }

    private List<TrackedMovie> toDomainMovies(StorageData storageData) throws StorageException {
        if (storageData == null) {
            throw new StorageException("Stored movie data is empty");
        }
        if (storageData.version() != CURRENT_FORMAT_VERSION) {
            throw new StorageException("Unsupported storage version: " + storageData.version());
        }
        if (storageData.movies() == null) {
            throw new StorageException("Stored movie list is missing");
        }

        List<TrackedMovie> movies = new ArrayList<>(storageData.movies().size());
        for (TrackedMovieData data : storageData.movies()) {
            if (data == null) {
                throw new StorageException("Stored movie entry is missing");
            }
            movies.add(toDomainMovie(data));
        }
        return List.copyOf(movies);
    }

    private TrackedMovie toDomainMovie(TrackedMovieData data) {
        LocalDate releaseDate = data.releaseDate() == null
                ? null
                : LocalDate.parse(data.releaseDate());
        Movie movie = new Movie(data.tmdbId(), data.title(), releaseDate, data.posterPath());
        WatchStatus status = WatchStatus.valueOf(data.status());
        return new TrackedMovie(movie, status, data.personalRating());
    }

    private StorageData toStorageData(Collection<TrackedMovie> movies) {
        List<TrackedMovieData> storedMovies = movies.stream()
                .map(LocalStorage::toTrackedMovieData)
                .toList();
        return new StorageData(CURRENT_FORMAT_VERSION, storedMovies);
    }

    private static TrackedMovieData toTrackedMovieData(TrackedMovie trackedMovie) {
        Objects.requireNonNull(trackedMovie, "trackedMovie");
        Movie movie = trackedMovie.getMovie();
        return new TrackedMovieData(
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getReleaseDate().map(LocalDate::toString).orElse(null),
                movie.getPosterPath().orElse(null),
                trackedMovie.getStatus().name(),
                trackedMovie.getPersonalRating().isPresent()
                        ? trackedMovie.getPersonalRating().getAsInt()
                        : null);
    }

    private void replaceDataFile(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, dataFile,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteTemporaryFile(Path temporaryFile) {
        if (temporaryFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException ignored) {
            // Preserve the original storage failure; a stale temporary file is not authoritative data.
        }
    }
}
