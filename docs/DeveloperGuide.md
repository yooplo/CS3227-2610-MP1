# Movie Tracker Developer Guide

Movie Tracker is still under development. Movie search, details, Watchlist, Watched, and local collection persistence are connected to the graphical interface.

## Current architecture

The application uses Java 21, Gradle, JavaFX, and FXML. `Launcher` starts `MovieTrackerApplication`, which creates one production `TmdbMovieApiService`, loads one session-scoped `MovieCollection`, and wraps the collection and storage in one `MovieCollectionManager`. The service and manager are injected into `MainController` through the `FXMLLoader` controller factory.

The core domain contains application-level movie information, saved movies, watch statuses, and collection operations. JUnit 5 tests cover the domain and service layers.

## TMDB integration

`MovieApiService` defines application-level search and movie-detail operations. `TmdbMovieApiService` implements those operations with Java's `HttpClient`, authenticates with the `TMDB_API_TOKEN` environment variable, and uses Jackson to convert TMDB JSON into `MovieInfo`. TMDB-specific parsing remains inside the service package.

The service converts network, timeout, response, authentication, rate-limit, not-found, and other HTTP failures into `MovieServiceException`. Automated tests use local JSON and do not require a token or network access.

HTTP 408 and 504 responses are classified as timeouts. Successful responses are still validated: a null body, malformed JSON, missing required ID/title fields, invalid metadata types, invalid dates, and external ratings outside TMDB's 0–10 scale become `INVALID_RESPONSE` failures.

## Search GUI

`MainController` depends on the `MovieApiService` interface and contains no TMDB-specific HTTP or JSON handling. It runs searches in a JavaFX `Task` on a daemon background thread. JavaFX task handlers then update loading state, feedback, and result controls on the JavaFX Application Thread.

`MovieSearchMessages` converts service failure types into user-facing text without exposing low-level errors. Its mapping is covered by automated tests. The remaining JavaFX interaction is verified manually.

The interface uses JavaFX layout containers rather than fixed coordinates. `BorderPane` divides the header and main content, page-level `VBox` containers grow vertically, and fitted `ScrollPane` controls handle result or collection overflow. Search-card graphics bind their preferred width to the available button width so titles and overviews wrap as the window changes size. Collection actions and metadata use `FlowPane` to wrap naturally.

The stage has a 700×600 minimum size, preventing the poster-and-text layouts from being compressed below their usable desktop arrangement. Above that minimum, pages grow to a maximum readable content width, scrollable areas absorb additional height, and poster boxes retain fixed 2:3 thumbnail bounds while their `ImageView` nodes preserve source aspect ratio. No additional width breakpoint is currently necessary because these layout containers reflow continuously throughout the supported range.

## Movie details GUI

Search results and saved-movie cards call one shared details-loading method with the movie's TMDB ID and their originating view. The method hides the origin without clearing it and starts a background JavaFX `Task` that calls `MovieApiService.getMovieDetails`. Success and failure handlers update the details view on the JavaFX Application Thread; controllers contain no TMDB HTTP or JSON logic.

The controller tracks the active detail request. Re-selecting the same movie while it is loading does not issue a duplicate request, and a late result from an older request cannot overwrite a newer movie selection. View visibility is updated through one shared helper so Search, Details, Watchlist, and Watched cannot accidentally remain managed at the same time.

The controller stores the originating `View` when details are opened. The Back action and selected navigation state therefore return to Search, Watchlist, or Watched as appropriate. Returning to Search only toggles view visibility, preserving the previous query and result controls without another API search. The details view displays safe fallback text for missing dates, ratings, and overviews; `MovieDetailsText` keeps that formatting logic independently testable.

## Poster images

The domain continues to store only TMDB's relative poster path. `PosterImageLoader` constructs presentation URLs using `https://image.tmdb.org/t/p/w342` and never writes a full URL back into `MovieInfo`, `Movie`, or persistent JSON.

Posters use JavaFX `Image` background loading, so image retrieval does not block the JavaFX Application Thread. Each `ImageView` preserves the source aspect ratio and fits within view-specific bounds: compact search results, a larger details image, and consistent collection thumbnails.

Every poster container starts with a labeled placeholder. The image is revealed only after loading completes successfully; missing paths, malformed paths, constructor failures, and background download errors leave the same placeholder visible without exposing an exception to the user.

One `PosterImageLoader` belongs to the session-scoped `MainController`. It maintains a small least-recently-used cache of at most 100 JavaFX `Image` instances keyed by the constructed URL. Search, Details, Watchlist, and Watched can therefore reuse an image already requested during the same run. The cache is memory-only and is discarded at exit; poster files are never added to `data/movies.json` or written elsewhere on disk.

URL-construction tests cover normal, missing, unsafe, and malformed poster paths without creating images or accessing the network. Image download success and failure remain manual GUI checks.

## Watchlist

`MovieFactory` performs the single application-level conversion from `MovieInfo` into a saved `Movie`, copying all current metadata and assigning `WatchStatus.WATCHLIST`. `MovieCollection.add` uses the TMDB ID to reject duplicates without replacing the existing movie or status.

The Watchlist view is refreshed from `MovieCollection.getWatchlistMovies` whenever it is opened and after a collection mutation. Its movie-information area is a full-width, keyboard-focusable region with an accessible button role that opens details, while Mark as Watched and Remove are separate sibling controls. Using a content-sized region avoids JavaFX `ButtonSkin` stretching card graphics vertically. An action-button click therefore performs only its mutation and cannot bubble into details navigation. Each mutation captures the entry's TMDB ID, delegates to `MovieCollectionManager`, and refreshes the relevant filtered views. Search controls and result nodes remain in memory while the Watchlist view is displayed.

## Watched movies

Both Watchlist-card and details-page Mark as Watched actions delegate the one-way status transition to `MovieCollectionManager.markAsWatched`, which applies domain behavior through `MovieCollection` and invokes the existing persistence flow. The controller then refreshes Watchlist from `getWatchlistMovies` and Watched from `getWatchedMovies`, so the same domain object moves between the filtered views without being duplicated or replaced. A save failure preserves the in-memory transition and produces the established safe storage warning.

When details are opened, `MovieDetailsCollectionState` classifies the shared collection lookup as unsaved, `WATCHLIST`, or `WATCHED`. Unsaved movies show both Add to Watchlist and Mark as Watched. The latter uses `MovieFactory` to create the saved movie directly with `WATCHED` status and persists it through one `MovieCollectionManager.add` mutation. Watchlist movies show an already-saved message and use the existing `markAsWatched` mutation; watched movies show an already-watched message and neither active status action. State and status-aware conversion helpers are covered by non-network unit tests. No watched-to-watchlist transition is exposed.

## Local storage

`MovieStorage` owns Jackson and file I/O. Production uses the OS-independent relative path `Path.of("data", "movies.json")`. The JSON document has a format version and a `movies` array. Each movie stores its TMDB ID, title, ISO-8601 release date (or `null`), overview, poster path, external rating, and `WATCHLIST` or `WATCHED` status. Storage DTOs are private to the storage class; Jackson types do not enter the domain or controller layers.

At startup, `MovieTrackerApplication` calls `MovieStorage.load` before constructing the controller. A missing directory or file is a normal first-launch condition and produces an empty collection. The directory and file are created on the first successful save.

`MovieCollectionManager` delegates add, remove, and mark-as-watched behavior to the single `MovieCollection`. After each successful domain mutation it immediately asks `MovieStorage` to save the complete collection. Duplicate additions and other no-change operations do not write. The controller receives only a mutation result and displays safe feedback; it performs no file I/O.

Saving first serializes to a temporary file in the same directory and then replaces `movies.json`, using an atomic move where the file system supports it. A save failure leaves the in-memory change available for the current session and displays a general warning rather than crashing or exposing file-system details.

Malformed JSON, unsupported format versions, incorrectly typed scalar values, invalid movie fields, invalid statuses, invalid dates, external ratings outside 0–10, and duplicate TMDB IDs are treated as corrupted storage. `MovieTrackerApplication` then starts with an empty in-memory collection and disables persistence for that run. This preserves the existing file and prevents later user actions from accidentally overwriting it. The UI keeps working and displays a persistent warning explaining that changes are session-only. `Movie` and `MovieInfo` share the same validation for TMDB identity, title, and external rating, preventing invalid application objects from later producing unreadable saved data.

Storage tests use JUnit temporary directories, never the production `data/movies.json`, and cover empty and multiple-movie saves, status preservation, missing paths, optional metadata, round trips, corruption, invalid data, failed saves, and mutation-triggered persistence. They do not access TMDB.

## Acknowledgements

The project currently depends on JavaFX, Jackson, JUnit 5, Gradle, and the Foojay Gradle toolchain resolver. Movie metadata is provided by [The Movie Database (TMDB)](https://www.themoviedb.org/); this product is not endorsed or certified by TMDB.
