package movietracker.model;

final class MovieValidation {
    private MovieValidation() {
    }

    static void validateIdentityAndMetadata(int tmdbId, String title, Double externalRating) {
        if (tmdbId <= 0) {
            throw new IllegalArgumentException("TMDB movie ID must be positive");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Movie title must not be blank");
        }
        if (externalRating != null
                && (!Double.isFinite(externalRating) || externalRating < 0.0 || externalRating > 10.0)) {
            throw new IllegalArgumentException("External rating must be between 0 and 10");
        }
    }
}
