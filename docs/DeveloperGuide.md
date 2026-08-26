# Movie Tracker Developer Guide

## Overview

Movie Tracker is a Java 25 desktop application built with JavaFX and Gradle. It uses TMDB as a read-only movie metadata provider and stores the user's tracking state in a local versioned JSON file.

The implementation uses a small layered architecture:

```text
Launcher -> MovieTrackerApp (composition root)
                         |
                         v
                  JavaFX UI views
                         |
                         v
          MovieTrackerApplicationService
                  /                 \
                 v                   v
           TmdbClient       MovieTrackerService
                 |                   |
           TMDB HTTPS API       Storage interface
                                     |
                                LocalStorage
```

Domain models are shared by the API, service, storage, and UI boundaries without depending on JavaFX, HTTP, Jackson, or file I/O.

## Application startup and composition

`movietracker.Launcher` is the plain Java executable entry point and delegates to `MovieTrackerApp`. Keeping the launcher separate avoids using a JavaFX `Application` subclass directly as the executable JAR entry point.

`MovieTrackerApp` is the composition root. At startup it creates:

- `LocalStorage` using `data/movies.json`;
- `MovieTrackerService`, which immediately loads local tracking state;
- a lazy environment-configured `TmdbClient`;
- `MovieTrackerApplicationService`;
- one daemon, single-thread application executor;
- `MainWindow`, with the application service and executor injected.

Views do not instantiate the API client, storage, or tracking service. `MovieTrackerApp.stop()` cancels view tasks and shuts down the executor.

Missing storage is a normal empty first run. If existing storage cannot be loaded, `MovieTrackerApp` displays a blocking, non-technical error and exits without constructing an empty tracking service. This prevents later mutations from overwriting corrupted, unsupported, or unreadable data. Missing TMDB configuration is deliberately different: it is checked when a TMDB request starts, so it does not prevent the main window from opening.

## Layers and responsibilities

### JavaFX UI

The UI is programmatic JavaFX styled by `src/main/resources/movietracker/css/app.css`; it does not use FXML.

- `MainWindow` owns the single-window shell, active navigation state, and origin-aware Details navigation.
- `SearchView` validates input, starts searches, renders title/year results, and retains its current query/results when another view is temporarily shown.
- `MovieDetailsView` asynchronously loads details and delegates tracking and rating actions.
- `WatchlistView` and `WatchedView` refresh from local service snapshots whenever their section is shown. Their rows open Details and perform removal.
- `PosterView` builds on `TmdbImageUrls` and JavaFX background image loading, with a non-fatal placeholder.
- `TmdbErrorMessages` and `UiErrorMessages` centralize safe user-facing text.

Network calls and persistence-backed UI mutations run in JavaFX `Task` instances on the injected executor, not on the JavaFX Application Thread. Task handlers publish success, failure, and cancellation states back on the JavaFX thread. Views guard against duplicate in-flight actions; Details tasks are cancelled when leaving Details, and all active view tasks are cancelled during shutdown. Poster loading uses JavaFX's background-loading `Image` constructor.

### Application coordination

`MovieTrackerApplicationService` is the single API exposed to JavaFX views. It is synchronous and intentionally thin:

- Search and Details delegate to `TmdbClient`.
- Tracking queries and mutations delegate to `MovieTrackerService`.
- `TmdbException` and `StorageException` remain structured and keep their causes; the coordinator does not convert them to UI controls or technical strings.

Background execution is a UI integration concern, not a responsibility of this service.

### Tracking service

`MovieTrackerService` owns an immutable snapshot list of `TrackedMovie` objects in deterministic insertion order. It:

- filters Watchlist and Watched state;
- detects duplicates by TMDB ID;
- adds to Watchlist;
- adds an untracked movie directly to Watched or moves a Watchlist movie to Watched;
- removes any tracked movie;
- sets, updates, or clears a watched movie's 1–10 personal rating.

Mutations use a **persist-before-publish** strategy. The service constructs an immutable proposed state, calls `Storage.save`, and replaces its in-memory state only after the save succeeds. A failed save therefore leaves both the public in-memory state and the UI's last confirmed state unchanged. Rejected duplicates and other no-op mutations are not saved.

### TMDB API layer

`TmdbClient` uses Java's `java.net.http.HttpClient` through the injectable `HttpTransport` boundary. `JdkHttpTransport` is the production implementation; tests substitute an offline transport. Requests use TMDB API v3 over HTTPS, Bearer authentication, a 10-second connect timeout, and a 15-second request timeout.

`TmdbClient.fromEnvironment()` installs a configuration provider rather than reading the environment immediately. Each non-blank request resolves `TMDB_API_TOKEN`, so missing configuration is reported at request time. Tokens and authorization headers are not included in exceptions or UI messages.

Jackson maps raw snake-case JSON into records under `movietracker.api.dto`. DTOs are then validated and mapped into `Movie` or `MovieDetails`; Jackson annotations and raw JSON objects do not leak into domain classes. Search-result overview and vote fields may exist in the TMDB DTO, but the current lightweight `Movie` domain object intentionally retains only ID, title, optional release date, and optional poster path.

`TmdbImageUrls` owns construction of the TMDB `w342` image URL so complete image URLs are not embedded in domain models or scattered through views.

### Storage layer

`Storage` defines whole-state `load` and `save` operations. `LocalStorage` resolves `Path.of("data", "movies.json")` against the process working directory and normalizes it to an absolute path internally.

Loading a missing file returns an immutable empty list without creating the directory. Saving creates missing parent directories, serializes to a temporary file in the same directory, and replaces the authoritative file with an atomic move when supported. If atomic moves are unavailable, it falls back to a replace move.

The current JSON format is version 1:

```json
{
  "version" : 1,
  "movies" : [ {
    "tmdbId" : 550,
    "title" : "Example Movie",
    "releaseDate" : "1999-10-15",
    "posterPath" : "/example.jpg",
    "status" : "WATCHLIST",
    "personalRating" : null
  } ]
}
```

Optional values are JSON `null`. `status` is `WATCHLIST` or `WATCHED`; only a `WATCHED` entry may have an integer `personalRating` from 1 to 10. `StorageData` and `TrackedMovieData` keep the JSON representation separate from the domain model. `MovieDetails`, TMDB response bodies, API credentials, and timestamps are not persisted.

Malformed JSON, empty files, unsupported versions, invalid domain data, and file access failures cross the storage boundary as `StorageException`. Load failures do not modify the source file.

### Domain model

The domain model is immutable:

- `Movie` is a lightweight metadata snapshot.
- `MovieDetails` composes a `Movie` with overview, optional runtime, immutable genres, backdrop path, and optional TMDB vote average.
- `TrackedMovie` composes a `Movie` with `WatchStatus` and an optional personal rating.
- `WatchStatus` contains the mutually exclusive `WATCHLIST` and `WATCHED` states.

TMDB ID is the identity of `Movie`, `MovieDetails`, and `TrackedMovie`. Equality and hash codes therefore remain stable when titles, poster paths, details, tracking status, or ratings change. This matches duplicate detection and ensures one local tracking entry per external TMDB movie.

Constructors enforce genuine invariants, including positive IDs, non-blank titles, valid optional metadata, non-negative runtime, a 0–10 TMDB vote average, and watched-only personal ratings from 1 to 10. Collections are defensively copied and service query results are unmodifiable.

## Error handling

`TmdbException` is the checked TMDB boundary. It carries a `TmdbErrorCategory` and, for HTTP failures, an optional status code:

- `MISSING_TOKEN`
- `NETWORK`
- `TIMEOUT`
- `HTTP_ERROR`
- `INVALID_RESPONSE`
- `INTERRUPTED`

Underlying I/O, timeout, parsing, and interruption causes are preserved where useful for debugging, but raw response bodies and credentials are excluded. `TmdbErrorMessages` maps categories to stable Search/Details messages without parsing exception text.

`StorageException` is the checked local persistence boundary. `UiErrorMessages` provides non-technical tracking-save and startup messages. The service's persist-before-publish behavior prevents a failed mutation from becoming visible as successful. Existing load failures are blocked at the composition root to prevent silent data loss.

Domain misuse uses `IllegalArgumentException`, `IllegalStateException`, or null checks. The current JavaFX controls only expose valid rating values for watched movies, while the domain and service remain authoritative.

## Testing strategy

JUnit 5 tests mirror production packages under `src/test/java`. The automated suite covers:

- domain construction, validation, immutability, identity, and rating boundaries;
- storage first-run behavior, round trips, directory creation, malformed/empty/unsupported data, invalid domain data, and non-destructive failures using JUnit temporary directories;
- TMDB request construction, authentication, fixture mapping, blank queries, optional fields, HTTP/network/timeout categories, and malformed responses through injected transports; user-message tests cover every category, including interruption;
- tracking transitions, duplicate prevention, filtering, ratings, removals, immutable snapshots, persistence calls, and rollback using fake storage;
- application-service delegation and exception propagation;
- composition-boundary startup failure propagation and stable user-message mappings.

Tests never call live TMDB and never write to the repository's real `data` directory. There is no brittle JavaFX UI automation; Search/Details interactions, resizing, poster behavior, the end-to-end tracking flow, and platform launch behavior require manual smoke testing.

Run the suite with:

```powershell
.\gradlew.bat test
```

## Build and release packaging

The Gradle `releaseJar` task runs tests, removes stale `release/*.jar` files, and builds `release/MovieTracker.jar`. It is an executable fat JAR whose manifest uses `movietracker.Launcher`; it includes application classes/resources, Jackson, and JavaFX runtime dependencies.

JavaFX native libraries are platform- and architecture-specific. Consequently, one fat JAR is not genuinely universal. The checked-in release artifact is currently **Windows x86-64 only**. The build accepts `-PjavafxPlatform=win`, `linux`, `linux-aarch64`, `mac`, or `mac-aarch64`, but artifacts must be separately built and smoke-tested on their matching systems. Linux and macOS releases are not currently verified, so no cross-platform runtime claim is made for the checked-in JAR.

The packaged application continues to read `TMDB_API_TOKEN` from its process environment and writes `data/movies.json` relative to its launch working directory. Tokens, source files, tests, fixtures, and local data are not intentionally packaged in the JAR.

## Acknowledgements

- Movie metadata and images are provided through [The Movie Database (TMDB) API](https://developer.themoviedb.org/docs/getting-started). This product uses the TMDB API but is not endorsed or certified by TMDB. The current attribution requirements are documented in the [TMDB API FAQ](https://developer.themoviedb.org/docs/faq).
- The GUI uses [OpenJFX/JavaFX](https://openjfx.io/) and its platform-specific runtime components.
- JSON serialization and mapping use [Jackson Databind](https://github.com/FasterXML/jackson-databind).
- Builds, tests, dependency resolution, and release assembly use [Gradle](https://gradle.org/).
- The project specification cites the compact, single-window JavaFX style and layered OOP direction of CS2103/T iP projects as design inspiration. This is conceptual inspiration; no external CS2103/T source code or graphical assets were identified in the implementation.

### AI-use acknowledgement

OpenAI Codex was used as an AI-assisted development tool for repository inspection, implementation and test suggestions, debugging, release-packaging work, and documentation drafting. Development was directed and reviewed incrementally against the project specifications; the student remains responsible for verifying, understanding, and submitting the final work. No AI-generated API credential or secret is stored in the repository.
