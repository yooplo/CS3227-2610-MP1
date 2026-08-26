package movietracker.api.dto;

import java.util.List;

/**
 * TMDB search response representation.
 */
public record TmdbSearchResponseDto(List<TmdbMovieDto> results) {
}
