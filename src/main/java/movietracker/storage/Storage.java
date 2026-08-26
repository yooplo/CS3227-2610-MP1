package movietracker.storage;

import java.util.Collection;
import java.util.List;

import movietracker.model.TrackedMovie;

/**
 * Persistence boundary for the complete tracked-movie state.
 */
public interface Storage {

    /**
     * Loads all tracked movies.
     *
     * @return immutable snapshot of the stored movies
     * @throws StorageException if existing data cannot be read or reconstructed
     */
    List<TrackedMovie> load() throws StorageException;

    /**
     * Replaces the stored tracked-movie state.
     *
     * @param movies complete state to save
     * @throws StorageException if the state cannot be saved
     */
    void save(Collection<TrackedMovie> movies) throws StorageException;
}
