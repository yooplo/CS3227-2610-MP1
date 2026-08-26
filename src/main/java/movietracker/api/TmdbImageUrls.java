package movietracker.api;

import java.net.URI;
import java.util.Optional;

/**
 * Builds TMDB image URLs outside the domain and UI layers.
 */
public final class TmdbImageUrls {

    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w342";

    private TmdbImageUrls() {
    }

    /**
     * Builds a w342 poster URI from a TMDB image path.
     *
     * @param posterPath TMDB poster path, or {@code null}
     * @return poster URI, or empty when no usable path is available
     */
    public static Optional<URI> posterUri(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return Optional.empty();
        }
        String normalizedPath = posterPath.trim();
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        return Optional.of(URI.create(POSTER_BASE_URL + normalizedPath));
    }
}
