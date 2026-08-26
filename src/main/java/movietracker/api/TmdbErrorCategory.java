package movietracker.api;

/**
 * Stable categories that allow higher layers to translate TMDB failures without parsing messages.
 */
public enum TmdbErrorCategory {
    MISSING_TOKEN,
    NETWORK,
    TIMEOUT,
    HTTP_ERROR,
    INVALID_RESPONSE,
    INTERRUPTED
}
