package movietracker.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import movietracker.model.MovieInfo;
import movietracker.service.MovieServiceException.FailureType;

final class FakeMovieApiService implements MovieApiService {
    private List<MovieInfo> searchResults = List.of();
    private final Map<Integer, MovieInfo> detailsByTmdbId = new HashMap<>();
    private MovieServiceException searchFailure;
    private MovieServiceException detailsFailure;

    void setSearchResults(List<MovieInfo> searchResults) {
        this.searchResults = List.copyOf(Objects.requireNonNull(searchResults));
    }

    void addMovieDetails(MovieInfo movieInfo) {
        Objects.requireNonNull(movieInfo, "movieInfo must not be null");
        detailsByTmdbId.put(movieInfo.tmdbId(), movieInfo);
    }

    void setSearchFailure(MovieServiceException searchFailure) {
        this.searchFailure = Objects.requireNonNull(searchFailure);
    }

    void setDetailsFailure(MovieServiceException detailsFailure) {
        this.detailsFailure = Objects.requireNonNull(detailsFailure);
    }

    @Override
    public List<MovieInfo> searchMovies(String query) throws MovieServiceException {
        Objects.requireNonNull(query, "query must not be null");
        if (searchFailure != null) {
            throw searchFailure;
        }
        return searchResults;
    }

    @Override
    public MovieInfo getMovieDetails(int tmdbId) throws MovieServiceException {
        if (detailsFailure != null) {
            throw detailsFailure;
        }

        MovieInfo movieInfo = detailsByTmdbId.get(tmdbId);
        if (movieInfo == null) {
            throw new MovieServiceException(
                    FailureType.NOT_FOUND,
                    "No movie found with TMDB ID " + tmdbId);
        }
        return movieInfo;
    }
}
