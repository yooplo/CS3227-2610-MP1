# Movie Tracker — Design Specification

## 1. Architecture overview

Use a small layered architecture so that JavaFX, TMDB networking, local persistence, and movie-tracking rules remain separate.

```text
JavaFX UI
   |
   v
Application / Service layer
   |--------------------|
   v                    v
TMDB client         Local storage
   |                    |
   v                    v
TMDB HTTPS API       Local data file

Shared domain models sit between these layers.
```

The goal is not to over-engineer MP1. The separation exists to make the code testable and to follow the OOP direction of the CS2103/T iP.

## 2. Recommended package structure

Keep `src/main/java` as the source root.

```text
src/main/java/
└── movietracker/
    ├── Launcher.java
    ├── MovieTrackerApp.java
    ├── model/
    │   ├── Movie.java
    │   ├── MovieDetails.java
    │   ├── TrackedMovie.java
    │   └── WatchStatus.java
    ├── service/
    │   ├── MovieTrackerApplicationService.java
    │   └── MovieTrackerService.java
    ├── api/
    │   ├── TmdbClient.java
    │   ├── TmdbConfig.java
    │   ├── TmdbException.java
    │   └── dto/
    ├── storage/
    │   ├── Storage.java
    │   ├── LocalStorage.java
    │   └── StorageException.java
    └── ui/
        ├── MainWindow.java
        ├── SearchView.java
        ├── MovieDetailsView.java
        └── TmdbErrorMessages.java
```

FXML files, if used:

```text
src/main/resources/
└── movietracker/
    ├── view/
    │   ├── MainWindow.fxml
    │   ├── SearchView.fxml
    │   ├── CollectionView.fxml
    │   └── MovieDetailView.fxml
    └── css/
        └── app.css
```

Tests mirror package structure under `src/test/java`.

## 3. Main classes and responsibilities

### `Launcher`

A small non-JavaFX launcher with `public static void main(String[] args)` that calls `MovieTrackerApp.main(args)` or `Application.launch(...)`.

Keep this separate because executable JAR launching with JavaFX can otherwise be troublesome depending on packaging.

### `MovieTrackerApp`

- JavaFX `Application` entry point.
- Creates long-lived dependencies such as `TmdbClient`, `LocalStorage`, and `MovieTrackerService`.
- Loads the main scene/window.
- Does not contain search/business/storage logic.

### `Movie`

Lightweight TMDB movie representation suitable for search results:

- `tmdbId`
- `title`
- `releaseDate`
- `posterPath`
- `overview` (optional for search result)
- `tmdbVoteAverage` (optional)

### `MovieDetails`

Detailed representation for the detail screen. May extend compositionally from `Movie` or contain common fields plus:

- runtime
- genres
- backdrop/poster path
- full overview
- vote average/count as desired

### `TrackedMovie`

Local persisted representation:

- movie snapshot fields
- `WatchStatus`
- optional personal rating
- optional `addedAt` / `watchedAt`

### `WatchStatus`

Java enum:

```text
WATCHLIST
WATCHED
```

### `MovieTrackerApplicationService`

Application-facing coordinator used by future JavaFX controllers. It exposes:

- `searchMovies(query)`
- `getMovieDetails(tmdbId)`
- `getWatchlist()`
- `getWatched()`
- `addToWatchlist(movie)`
- `markWatched(movie)`
- `removeTrackedMovie(tmdbId)`
- `setPersonalRating(tmdbId, rating)`

It delegates remote lookup to `TmdbClient` and local operations to
`MovieTrackerService`. It remains synchronous; the JavaFX layer must arrange
background execution for remote calls. `TmdbException` and `StorageException`
pass through unchanged so UI code can translate them without losing structured
error information or underlying causes.

### `MovieTrackerService`

Owns the in-memory tracked-movie collection, enforces tracking invariants such
as “a movie cannot be in both Watchlist and Watched”, and coordinates persistence
after successful mutations. It does not perform TMDB lookup or depend on JavaFX.

### `TmdbClient`

Only class/package responsible for raw HTTP interaction with TMDB.

Responsibilities:

- Build HTTPS requests.
- Add authentication header.
- URL-encode search terms.
- Decode JSON responses.
- Convert TMDB DTOs into domain models.
- Convert non-success responses/network failures into `TmdbException`.

It should not know about Watchlist or Watched.

### `LocalStorage`

Responsibilities:

- Load local tracked movies on startup.
- Save the complete tracking state after mutations.
- Create parent directories/file when absent.
- Handle malformed/corrupted data deliberately.
- Use OS-independent paths.

For MP1, JSON is recommended because it is human-readable and straightforward to version/debug.

Suggested path:

```text
./data/movies.json
```

## 4. UI design

### Main window

Compact single-window desktop UI with a left/top navigation region and one main content area.

Recommended sections:

- Search
- Watchlist
- Watched

The structure should feel similar in simplicity to the CS2103/T iP GUI, but adapted to movie cards/lists rather than chatbot bubbles.

The initial shell uses a programmatic JavaFX `MainWindow` rather than FXML. Its
left navigation swaps the center content among Search, Watchlist, and Watched
placeholders in the same scene. This keeps the small shell in one focused class;
feature-specific views or controllers can be introduced when those sections gain
behavior. Basic structural styling lives in `movietracker/css/app.css`.

`MovieTrackerApp` is the composition root: it creates `LocalStorage`,
`MovieTrackerService`, `TmdbClient`, `MovieTrackerApplicationService`, and the
application executor, then injects the application service and executor into the UI.
If existing local data cannot be loaded, startup shows a blocking, non-technical
error and exits cleanly without creating an empty tracking service. This preserves
the original file and prevents later mutations from overwriting corrupted,
unsupported, or unreadable data. A missing data file remains a normal empty first
run, while TMDB configuration remains request-time so a missing token does not
prevent the main window from opening.
`SearchView` uses a JavaFX `Task` on that executor so TMDB work never blocks the
JavaFX Application Thread. Task completion handlers update controls on the
JavaFX thread. Runtime TMDB configuration is resolved when a request begins, so
a missing token becomes a Search error state instead of preventing application
startup. `TmdbErrorMessages` maps stable error categories to safe UI text shared
by Search and movie details.

`MainWindow` owns Search-to-details navigation. It retains the same `SearchView`
instance while swapping the center content to a `MovieDetailsView`, preserving
the last query and results without another search request. Detail metadata is
loaded in a JavaFX `Task` on the shared application executor. Poster URLs are constructed
by `TmdbImageUrls` in the API layer, and JavaFX loads poster images in the
background with a non-fatal placeholder for missing or failed images.

`MovieDetailsView` queries current tracking status through
`MovieTrackerApplicationService` and delegates Watchlist additions to that same
boundary. Persistence-backed mutations run as JavaFX tasks on the shared
application executor so file I/O does not block the JavaFX Application Thread.
The service persists before publishing its new in-memory state; therefore the UI
shows Watchlist success only after the task succeeds and retains the prior state
when `StorageException` is reported.

`WatchlistView` renders the application's locally stored Watchlist snapshot and
refreshes whenever its navigation section is shown. Its rows reuse `PosterView`,
open the shared detail view, and remove movies through
`MovieTrackerApplicationService` on the application executor. `MainWindow`
records whether Details was opened from Search or Watchlist so Back restores the
originating view; returning to Watchlist refreshes it from current service state.
Removal leaves the displayed item in place until persistence succeeds.

### Search view

```text
+-------------------------------------------------------+
| Movie Tracker                         [Watchlist] [...]|
+-------------------------------------------------------+
| [ Search movies............................ ] [Search] |
|                                                       |
| [poster] Title (Year)                                 |
|          Short overview...                            |
| ----------------------------------------------------- |
| [poster] Another Movie (Year)                         |
+-------------------------------------------------------+
```

Search should run off the JavaFX UI thread. Disable/reduce duplicate requests while a request is in flight and show a loading indicator.

### Collection views

Watchlist and Watched can reuse the same controller/component with a different status filter.

Each movie row/card should be clickable and open the detail view.

### Movie detail view

```text
+-------------------------------------------------------+
| < Back                                                |
| [poster]  Movie Title (2026)                          |
|           Runtime | Genres | TMDB score               |
|                                                       |
|           Overview text...                            |
|                                                       |
| [Add to Watchlist] [Mark Watched] [Remove]            |
+-------------------------------------------------------+
```

Buttons should reflect current tracking state rather than allowing contradictory actions.

## 5. State and navigation

Keep navigation state simple. A single main controller can swap the center content or switch scenes/views.

Suggested state:

- active section
- last search query
- current search results
- selected movie ID

The persisted data model should not depend on JavaFX observable properties. UI-specific observable wrappers may be created in controllers if needed.

## 6. Concurrency

All HTTP/network work must happen away from the JavaFX Application Thread.

Acceptable approaches:

- `java.net.http.HttpClient` + `CompletableFuture`
- JavaFX `Task`
- an `ExecutorService`

UI controls are updated back on the JavaFX thread.

## 7. Persistence format

Recommended JSON shape:

```json
{
  "version": 1,
  "movies": [
    {
      "tmdbId": 550,
      "title": "Example",
      "releaseDate": "1999-10-15",
      "posterPath": "/example.jpg",
      "status": "WATCHLIST",
      "personalRating": null,
      "addedAt": "2026-08-26T17:00:00"
    }
  ]
}
```

Use a top-level `version` field so future storage changes can be handled intentionally.

## 8. Dependency direction rules

- `ui` may depend on `service` and `model`.
- `service` may depend on `api`, `storage`, and `model`.
- `api` may depend on `model`, but must not depend on `ui`.
- `storage` may depend on `model`, but must not depend on `ui` or `api`.
- `model` should have no JavaFX dependency.

## 9. Error model

Separate errors by concern:

- `TmdbException` — network, authentication, HTTP response, malformed TMDB payload.
- `StorageException` — read/write/corruption/access failure.
- `IllegalArgumentException` or domain-specific validation errors — invalid user/domain operation.

The UI translates technical exceptions into concise user-facing messages.

## 10. Testing strategy

Highest-value automated tests:

1. Watchlist/Watched transition rules in `MovieTrackerService`.
2. Duplicate prevention.
3. Storage round-trip serialization/deserialization.
4. Corrupted/missing storage handling.
5. TMDB JSON mapping using saved fixture JSON rather than live API calls.
6. Search query validation.

Do not make unit tests depend on the live TMDB service.

Manual GUI checks should cover:

- search and loading state
- movie detail navigation
- collection item click behavior
- resizing
- API-unavailable error state
- fresh-start behavior
- Windows/Linux/macOS smoke tests before release
