# Movie Tracker Developer Guide

Movie Tracker is still under development. Movie search, details, Watchlist, Watched, and local collection persistence are connected to the graphical interface.

## Current architecture

The application uses Java 21, Gradle, JavaFX, and FXML. `Launcher` starts `MovieTrackerApplication`, which creates one production `TmdbMovieApiService`, loads one session-scoped `MovieCollection`, and wraps the collection and storage in one `MovieCollectionManager`. The service and manager are injected into `MainController` through the `FXMLLoader` controller factory.

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

## Watchlist

`MovieFactory` performs the single application-level conversion from `MovieInfo` into a saved `Movie`, copying all current metadata and assigning `WatchStatus.WATCHLIST`. `MovieCollection.add` uses the TMDB ID to reject duplicates without replacing the existing movie or status.

The Watchlist view is refreshed from `MovieCollection.getWatchlistMovies` whenever it is opened and after removal. Each Remove action captures the entry's TMDB ID and calls `MovieCollection.remove`, then refreshes the view immediately. Search controls and result nodes remain in memory while the Watchlist view is displayed.

## Watched movies

The Watchlist action delegates the one-way status transition to `MovieCollection.markAsWatched`. It then refreshes the Watchlist from `getWatchlistMovies` and the Watched view from `getWatchedMovies`, so the same domain object moves between the filtered views without being duplicated or replaced.

When details are opened, the controller looks up the TMDB ID in the shared collection. A `WATCHED` movie disables Add to Watchlist and displays an already-watched message. No watched-to-watchlist transition is exposed.

## Local storage

`MovieStorage` owns Jackson and file I/O. Production uses the OS-independent relative path `Path.of("data", "movies.json")`. The JSON document has a format version and a `movies` array. Each movie stores its TMDB ID, title, ISO-8601 release date (or `null`), overview, poster path, external rating, and `WATCHLIST` or `WATCHED` status. Storage DTOs are private to the storage class; Jackson types do not enter the domain or controller layers.

At startup, `MovieTrackerApplication` calls `MovieStorage.load` before constructing the controller. A missing directory or file is a normal first-launch condition and produces an empty collection. The directory and file are created on the first successful save.

`MovieCollectionManager` delegates add, remove, and mark-as-watched behavior to the single `MovieCollection`. After each successful domain mutation it immediately asks `MovieStorage` to save the complete collection. Duplicate additions and other no-change operations do not write. The controller receives only a mutation result and displays safe feedback; it performs no file I/O.

Saving first serializes to a temporary file in the same directory and then replaces `movies.json`, using an atomic move where the file system supports it. A save failure leaves the in-memory change available for the current session and displays a general warning rather than crashing or exposing file-system details.

Malformed JSON, unsupported format versions, invalid movie fields, invalid statuses, invalid dates, and duplicate TMDB IDs are treated as corrupted storage. `MovieTrackerApplication` then starts with an empty in-memory collection and disables persistence for that run. This preserves the existing file and prevents later user actions from accidentally overwriting it. The UI keeps working and displays a persistent warning explaining that changes are session-only.

Storage tests use JUnit temporary directories, never the production `data/movies.json`, and cover empty and multiple-movie saves, status preservation, missing paths, optional metadata, round trips, corruption, invalid data, failed saves, and mutation-triggered persistence. They do not access TMDB.

## Acknowledgements

The project currently depends on JavaFX, Jackson, JUnit 5, Gradle, and the Foojay Gradle toolchain resolver. Movie metadata is provided by [The Movie Database (TMDB)](https://www.themoviedb.org/); this product is not endorsed or certified by TMDB.
