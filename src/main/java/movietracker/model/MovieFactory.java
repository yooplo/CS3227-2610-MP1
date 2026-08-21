package movietracker.model;

import java.util.Objects;

public final class MovieFactory {
    private MovieFactory() {
    }

    public static Movie fromMovieInfo(MovieInfo movieInfo) {
        return fromMovieInfo(movieInfo, WatchStatus.WATCHLIST);
    }

    public static Movie fromMovieInfo(MovieInfo movieInfo, WatchStatus watchStatus) {
        Objects.requireNonNull(movieInfo, "movieInfo must not be null");
        return new Movie(
                movieInfo.tmdbId(),
                movieInfo.title(),
                movieInfo.releaseDate(),
                movieInfo.overview(),
                movieInfo.posterPath(),
                movieInfo.externalRating(),
                watchStatus);
    }
}
