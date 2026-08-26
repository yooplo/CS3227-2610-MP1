package movietracker.api;

/**
 * Runtime TMDB configuration that keeps the read-access token out of source code.
 */
public final class TmdbConfig {

    public static final String TOKEN_ENVIRONMENT_VARIABLE = "TMDB_API_TOKEN";

    private final String readAccessToken;

    /**
     * Creates configuration from a runtime token.
     *
     * @param readAccessToken TMDB API Read Access Token
     * @throws TmdbException if the token is missing or blank
     */
    public TmdbConfig(String readAccessToken) throws TmdbException {
        if (readAccessToken == null || readAccessToken.isBlank()) {
            throw TmdbException.forMissingToken();
        }
        this.readAccessToken = readAccessToken.trim();
    }

    /**
     * Reads configuration from the process environment.
     *
     * @return validated TMDB configuration
     * @throws TmdbException if the environment variable is missing or blank
     */
    public static TmdbConfig fromEnvironment() throws TmdbException {
        return new TmdbConfig(System.getenv(TOKEN_ENVIRONMENT_VARIABLE));
    }

    String authorizationHeaderValue() {
        return "Bearer " + readAccessToken;
    }
}
