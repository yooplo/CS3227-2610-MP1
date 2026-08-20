package movietracker.model;

import java.time.LocalDate;

public record MovieInfo(
        int tmdbId,
        String title,
        LocalDate releaseDate,
        String overview,
        String posterPath,
        Double externalRating) {

    public MovieInfo {
        MovieValidation.validateIdentityAndMetadata(tmdbId, title, externalRating);
    }
}
