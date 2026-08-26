package movietracker.storage;

import java.util.List;

/**
 * JSON-specific persistence representations, kept separate from domain models.
 */
record StorageData(int version, List<TrackedMovieData> movies) {
}

record TrackedMovieData(
        int tmdbId,
        String title,
        String releaseDate,
        String posterPath,
        String status,
        Integer personalRating) {
}
