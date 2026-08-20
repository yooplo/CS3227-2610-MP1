package movietracker.service;

import java.util.List;

import movietracker.model.MovieInfo;

public interface MovieApiService {
    List<MovieInfo> searchMovies(String query) throws MovieServiceException;

    MovieInfo getMovieDetails(int tmdbId) throws MovieServiceException;
}
