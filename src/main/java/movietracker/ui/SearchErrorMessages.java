package movietracker.ui;

import java.util.Objects;

import movietracker.api.TmdbErrorCategory;

/**
 * Converts stable TMDB error categories into safe Search-view messages.
 */
final class SearchErrorMessages {

    private SearchErrorMessages() {
    }

    static String forCategory(TmdbErrorCategory category) {
        Objects.requireNonNull(category, "category");
        return switch (category) {
        case MISSING_TOKEN -> "TMDB API token is not configured.";
        case NETWORK -> "Unable to connect to TMDB. Check your connection and try again.";
        case TIMEOUT -> "TMDB took too long to respond. Try again.";
        case HTTP_ERROR -> "TMDB returned an error. Try again later.";
        case INVALID_RESPONSE -> "TMDB returned invalid data. Try again later.";
        case INTERRUPTED -> "The TMDB request was interrupted. Try again.";
        };
    }
}
