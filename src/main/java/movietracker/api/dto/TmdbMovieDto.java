package movietracker.api.dto;

/**
 * Lightweight movie representation returned by TMDB search.
 */
public record TmdbMovieDto(
        Integer id,
        String title,
        String releaseDate,
        String posterPath,
        String overview,
        Double voteAverage) {
}
