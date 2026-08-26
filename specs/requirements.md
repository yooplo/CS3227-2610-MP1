# Movie Tracker — Requirements Specification

## 1. Product goal

Movie Tracker is a Java desktop application that lets a user search for movies using TMDB, inspect movie details, add movies to a local watchlist, and track movies they have watched.

The UI should take inspiration from the simple, compact JavaFX desktop style used in the CS2103/T iP: a single desktop window, clear user actions, responsive content, and straightforward navigation rather than a web-style multi-page application.

## 2. Actors

### Primary actor

**User** — a single local user running the application on their desktop computer.

### External system

**TMDB API** — supplies movie search results, movie details, posters, and other read-only metadata.

## 3. Functional requirements

### FR-SEARCH — Search for movies

- **FR-SEARCH-01** The user can enter a movie title into a search field.
- **FR-SEARCH-02** The app queries TMDB and displays matching movies.
- **FR-SEARCH-03** Each search result should show at minimum the title and release year when available.
- **FR-SEARCH-04** Search results should show a poster thumbnail when TMDB provides one.
- **FR-SEARCH-05** The app must handle an empty search query without calling TMDB.
- **FR-SEARCH-06** The app must show a useful message when no results are found.
- **FR-SEARCH-07** The app must show a useful error state when TMDB cannot be reached or returns an error.

### FR-DETAIL — View movie details

- **FR-DETAIL-01** Clicking/selecting a movie from search results opens its movie-detail view.
- **FR-DETAIL-02** Clicking/selecting a movie from Watchlist opens the same movie-detail view.
- **FR-DETAIL-03** Clicking/selecting a movie from Watched opens the same movie-detail view.
- **FR-DETAIL-04** The detail view should display, when available: title, release date/year, poster, overview, runtime, genres, and TMDB rating.
- **FR-DETAIL-05** The user can return from the detail view to the previous collection/search context.

### FR-WATCHLIST — Maintain a watchlist

- **FR-WATCHLIST-01** The user can add a movie to Watchlist from the detail view.
- **FR-WATCHLIST-02** The app prevents duplicate entries for the same TMDB movie ID.
- **FR-WATCHLIST-03** The user can remove a movie from Watchlist.
- **FR-WATCHLIST-04** The Watchlist view displays all locally saved watchlist movies.
- **FR-WATCHLIST-05** Watchlist data persists after the app is closed and reopened.

### FR-WATCHED — Track watched movies

- **FR-WATCHED-01** The user can mark a movie as watched.
- **FR-WATCHED-02** Marking a Watchlist movie as watched moves it from Watchlist to Watched.
- **FR-WATCHED-03** A movie may not appear in both Watchlist and Watched simultaneously.
- **FR-WATCHED-04** The user can remove a movie from Watched.
- **FR-WATCHED-05** The Watched view displays all locally saved watched movies.
- **FR-WATCHED-06** Watched data persists after the app is closed and reopened.

### FR-RATING — Personal rating (recommended MVP+)

- **FR-RATING-01** The user can assign a personal integer rating from 1 to 10 to a watched movie.
- **FR-RATING-02** A personal rating is stored locally and is distinct from TMDB's public rating.
- **FR-RATING-03** The user can update or clear their personal rating.

> If implementation time is tight, FR-RATING may be deferred without affecting the core search/watchlist/watched flow.

### FR-NAV — Navigation

- **FR-NAV-01** The main UI provides direct access to Search, Watchlist, and Watched.
- **FR-NAV-02** The currently selected section is visually distinguishable.
- **FR-NAV-03** Navigation does not discard persisted tracking data.

### FR-STARTUP — Startup behavior

- **FR-STARTUP-01** The application starts even when its local data directory/file does not yet exist.
- **FR-STARTUP-02** Missing local storage is initialized automatically.
- **FR-STARTUP-03** Corrupted local data is handled gracefully; the app must not crash without a user-facing explanation.
- **FR-STARTUP-04** The TMDB token must not be hard-coded into committed source files.

## 4. Non-functional requirements

### NFR-PLATFORM

- **NFR-PLATFORM-01** The app is a Java desktop application.
- **NFR-PLATFORM-02** Default Java version is Java SE 25.
- **NFR-PLATFORM-03** The GUI is implemented with JavaFX.
- **NFR-PLATFORM-04** The application must function on Windows, Linux, and macOS.

### NFR-BUILD

- **NFR-BUILD-01** Gradle is used for build automation.
- **NFR-BUILD-02** `./gradlew test` (or `gradlew.bat test` on Windows) runs automated tests.
- **NFR-BUILD-03** A distributable JAR is generated into or copied into `release/` for submission.
- **NFR-BUILD-04** The release artifact must include the libraries required by the application according to MP1 submission instructions.

### NFR-CODE

- **NFR-CODE-01** Java source code remains under `src/main/java`.
- **NFR-CODE-02** Tests remain under `src/test/java`.
- **NFR-CODE-03** Code is organized into suitable Java packages.
- **NFR-CODE-04** UI, storage, API access, parsing/mapping, and domain logic should not be collapsed into one class.
- **NFR-CODE-05** Code should follow the SE-EDU Java coding standard where applicable.

### NFR-USABILITY

- **NFR-USABILITY-01** The UI should remain usable when the window is resized.
- **NFR-USABILITY-02** Network/loading/error/empty states should be clearly distinguishable.
- **NFR-USABILITY-03** A user should be able to complete the primary flow — search → details → watchlist → watched — without using a terminal.

### NFR-SECURITY

- **NFR-SECURITY-01** No TMDB secret/token is committed to Git.
- **NFR-SECURITY-02** API requests use HTTPS.
- **NFR-SECURITY-03** Logs and error messages must not print the full TMDB token.

## 5. Data rules

- TMDB movie ID is the stable identity for a movie in local tracking data.
- `WATCHLIST` and `WATCHED` are mutually exclusive states.
- A tracked movie stores enough metadata locally to render a useful collection view even before a fresh TMDB detail fetch succeeds.
- Suggested local snapshot fields: TMDB ID, title, release date/year, poster path, tracking status, personal rating (optional), date added, and date watched (optional).

## 6. Out of scope for MP1 MVP

Unless the spec is later revised:

- TMDB user login.
- Synchronizing with a user's TMDB watchlist/favourites.
- Social features.
- Multi-user accounts.
- TV-series tracking.
- Streaming-provider subscriptions.
- Recommendation algorithms.
- Cloud database/backend server.

## 7. Acceptance scenario

A build satisfies the core MVP when a fresh user can:

1. Launch the desktop app.
2. Search for a movie through TMDB.
3. Open a result and view its details.
4. Add it to Watchlist.
5. Close and reopen the app and still see it in Watchlist.
6. Open that Watchlist item and mark it Watched.
7. See it disappear from Watchlist and appear in Watched.
8. Close and reopen the app and still see the Watched state.
9. Receive understandable feedback if the network/API is unavailable.
