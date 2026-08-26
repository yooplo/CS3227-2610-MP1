package movietracker.model;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * An immutable locally tracked movie and its tracking state.
 */
public final class TrackedMovie {

    private static final int MINIMUM_RATING = 1;
    private static final int MAXIMUM_RATING = 10;

    private final Movie movie;
    private final WatchStatus status;
    private final Integer personalRating;

    /**
     * Creates a tracked movie.
     *
     * @param movie movie snapshot to track
     * @param status current tracking state
     * @param personalRating personal rating from 1 to 10, or {@code null}; only
     *                       watched movies may have a rating
     */
    public TrackedMovie(Movie movie, WatchStatus status, Integer personalRating) {
        this.movie = Objects.requireNonNull(movie, "movie");
        this.status = Objects.requireNonNull(status, "status");
        validatePersonalRating(status, personalRating);
        this.personalRating = personalRating;
    }

    public Movie getMovie() {
        return movie;
    }

    public WatchStatus getStatus() {
        return status;
    }

    public OptionalInt getPersonalRating() {
        return personalRating == null ? OptionalInt.empty() : OptionalInt.of(personalRating);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TrackedMovie trackedMovie)) {
            return false;
        }
        return movie.equals(trackedMovie.movie);
    }

    @Override
    public int hashCode() {
        return movie.hashCode();
    }

    @Override
    public String toString() {
        return "TrackedMovie{movie=" + movie + ", status=" + status
                + ", personalRating=" + personalRating + "}";
    }

    private static void validatePersonalRating(WatchStatus status, Integer personalRating) {
        if (personalRating == null) {
            return;
        }
        if (status != WatchStatus.WATCHED) {
            throw new IllegalArgumentException("Only watched movies may have a personal rating");
        }
        if (personalRating < MINIMUM_RATING || personalRating > MAXIMUM_RATING) {
            throw new IllegalArgumentException("Personal rating must be from 1 to 10");
        }
    }
}
