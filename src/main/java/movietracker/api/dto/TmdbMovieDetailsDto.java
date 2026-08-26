package movietracker.api.dto;

import java.util.List;

/**
 * Detailed movie representation returned by TMDB.
 */
public record TmdbMovieDetailsDto(
        Integer id,
        String title,
        String releaseDate,
        Integer runtime,
        List<TmdbGenreDto> genres,
        String overview,
        String posterPath,
        String backdropPath,
        Double voteAverage) {
}
