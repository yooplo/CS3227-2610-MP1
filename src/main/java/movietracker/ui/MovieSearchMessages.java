package movietracker.ui;

import movietracker.service.MovieServiceException;

final class MovieSearchMessages {
    private MovieSearchMessages() {
    }

    static String forSearchFailure(Throwable failure) {
        if (!(failure instanceof MovieServiceException serviceException)) {
            return "Unable to search for movies. Please try again.";
        }

        return switch (serviceException.getFailureType()) {
        case NETWORK -> "Unable to connect to TMDB. Check your internet connection and try again.";
        case TIMEOUT -> "The movie search timed out. Please try again.";
        case AUTHENTICATION -> "TMDB access is not configured or the token is invalid. "
                + "Set TMDB_API_TOKEN and restart Movie Tracker.";
        case RATE_LIMIT -> "Too many movie searches were requested. Please wait and try again.";
        case INVALID_RESPONSE -> "TMDB returned an unexpected response. Please try again later.";
        case HTTP_ERROR -> "TMDB could not complete the search. Please try again later.";
        case NOT_FOUND -> "The requested movie could not be found.";
        };
    }

    static String forDetailsFailure(Throwable failure) {
        if (!(failure instanceof MovieServiceException serviceException)) {
            return "Unable to load movie details. Please try again.";
        }

        return switch (serviceException.getFailureType()) {
        case NETWORK -> "Unable to connect to TMDB while loading movie details. "
                + "Check your internet connection and try again.";
        case TIMEOUT -> "Loading movie details timed out. Please try again.";
        case AUTHENTICATION -> "TMDB access is not configured or the token is invalid. "
                + "Set TMDB_API_TOKEN and restart Movie Tracker.";
        case RATE_LIMIT -> "Too many TMDB requests were made. Please wait and try again.";
        case NOT_FOUND -> "Movie details could not be found.";
        case INVALID_RESPONSE -> "TMDB returned unexpected movie details. Please try again later.";
        case HTTP_ERROR -> "TMDB could not load the movie details. Please try again later.";
        };
    }
}
