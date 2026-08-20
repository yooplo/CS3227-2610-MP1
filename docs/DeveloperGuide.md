# Movie Tracker Developer Guide

Movie Tracker is still under development. Movie search and movie details are connected to the graphical interface; watchlist workflows and persistence are not yet available through the GUI.

## Current architecture

The application uses Java 21, Gradle, JavaFX, and FXML. `Launcher` starts `MovieTrackerApplication`, which creates the production `TmdbMovieApiService` and injects it into `MainController` through the `FXMLLoader` controller factory.

The core domain contains application-level movie information, saved movies, watch statuses, and collection operations. JUnit 5 tests cover the domain and service layers.

## TMDB integration

`MovieApiService` defines application-level search and movie-detail operations. `TmdbMovieApiService` implements those operations with Java's `HttpClient`, authenticates with the `TMDB_API_TOKEN` environment variable, and uses Jackson to convert TMDB JSON into `MovieInfo`. TMDB-specific parsing remains inside the service package.

The service converts network, timeout, response, authentication, rate-limit, not-found, and other HTTP failures into `MovieServiceException`. Automated tests use local JSON and do not require a token or network access.

## Search GUI

`MainController` depends on the `MovieApiService` interface and contains no TMDB-specific HTTP or JSON handling. It runs searches in a JavaFX `Task` on a daemon background thread. JavaFX task handlers then update loading state, feedback, and result controls on the JavaFX Application Thread.

`MovieSearchMessages` converts service failure types into user-facing text without exposing low-level errors. Its mapping is covered by automated tests. The remaining JavaFX interaction is verified manually.

## Movie details GUI

Each search result is a selectable button carrying its application-level `MovieInfo`. Selecting it hides the search view without clearing it and starts a background JavaFX `Task` that calls `MovieApiService.getMovieDetails`. Success and failure handlers update the details view on the JavaFX Application Thread.

The details view displays safe fallback text for missing dates, ratings, and overviews. `MovieDetailsText` keeps that non-GUI formatting logic independently testable. Returning to the search view only toggles view visibility, preserving the previous query and result controls without another API search.

Persistence and user-facing watchlist workflows have not been implemented.

## Acknowledgements

The project currently depends on JavaFX, Jackson, JUnit 5, Gradle, and the Foojay Gradle toolchain resolver. Movie metadata is provided by [The Movie Database (TMDB)](https://www.themoviedb.org/); this product is not endorsed or certified by TMDB.
