# Movie Tracker — Implementation Task Plan

This task plan follows the CS2103/T-style incremental approach: implement in small steps, examine each change, test it, then commit it.

Use requirement IDs from `requirements.md` in commit/PR notes where useful.

## Phase 0 — Repository and build skeleton

- [x] Ensure the grading branch is named `master`.
- [x] Set Java toolchain/default target to Java SE 25.
- [x] Keep `src/main/java` as the Java source root.
- [x] Set up Gradle Wrapper.
- [x] Add JavaFX dependencies/plugin.
- [x] Add JUnit 5.
- [x] Add chosen JSON library.
- [x] Add `.gitignore` entries for IDE/build/temp/secret files.
- [x] Add `release/` directory policy.
- [x] Verify a minimal JavaFX window starts from Gradle.
- [x] Add separate `Launcher` class.

**Done when:** `gradlew run` launches an empty/minimal JavaFX Movie Tracker window on the development machine.

## Phase 1 — Local domain model

- [x] Add `Movie`.
- [x] Add `MovieDetails`.
- [x] Add `TrackedMovie`.
- [x] Add `WatchStatus` enum.
- [x] Write unit tests for status/data invariants.

**Done when:** domain classes compile and core invariants are tested without JavaFX or network code.

## Phase 2 — Local storage

- [x] Implement `Storage` interface.
- [x] Implement JSON `LocalStorage`.
- [x] Create missing `data/` directory/file automatically.
- [x] Load empty state on first run.
- [x] Save after every tracking mutation.
- [x] Add storage round-trip tests.
- [x] Add corrupted-file behavior and tests.

**Covers:** FR-WATCHLIST-05, FR-WATCHED-06, FR-STARTUP-01..03.

## Phase 3 — TMDB client happy path

- [x] Read `TMDB_API_TOKEN` from runtime configuration.
- [x] Implement authenticated GET helper.
- [x] Implement `/search/movie`.
- [x] Map search JSON into `Movie` objects.
- [x] Implement `/movie/{movie_id}`.
- [x] Map detail JSON into `MovieDetails`.
- [x] Add fixture-based mapping tests.

**Done when:** a non-GUI integration/manual runner can search and retrieve one movie detail without leaking the token.

## Phase 4 — TMDB error handling

- [x] Handle missing token.
- [x] Handle non-2xx responses.
- [x] Handle network failures/timeouts.
- [x] Handle malformed payloads.
- [x] Represent failures using `TmdbException`.
- [x] Ensure errors can be converted to user-friendly UI messages.

**Covers:** FR-SEARCH-07, NFR-SECURITY.

## Phase 5 — Core service logic

- [x] Implement `MovieTrackerService`.
- [x] Implement add-to-watchlist.
- [x] Prevent duplicates by TMDB ID.
- [x] Implement mark-watched transition.
- [x] Enforce Watchlist/Watched mutual exclusivity.
- [x] Implement remove.
- [x] Persist mutations.
- [x] Add comprehensive service tests.

**Covers:** FR-WATCHLIST, FR-WATCHED.

## Phase 5.5 — Application coordination

- [x] Add one application-facing API for TMDB lookup and local tracking.
- [x] Delegate lookup and tracking without duplicating client or business rules.
- [x] Preserve structured API and storage failures across the coordination layer.
- [x] Add offline coordination-layer unit tests.

**Done when:** future JavaFX controllers can use one injected application service
without coordinating the TMDB client and local tracking service directly.

## Phase 6 — Main JavaFX shell

- [x] Build the main window.
- [x] Add Search, Watchlist, Watched navigation.
- [x] Add CSS for compact desktop layout.
- [x] Make main content responsive to resizing.
- [x] Polish navigation, feedback states, controls, and collection/detail presentation.
- [x] Keep controllers thin and delegate behavior to service layer.

**Covers:** FR-NAV, NFR-USABILITY.

## Phase 7 — Search UI

- [x] Add search field/button.
- [x] Validate empty input locally.
- [x] Execute TMDB request off the JavaFX Application Thread.
- [x] Show loading state.
- [x] Render result rows/cards.
- [ ] Render poster or placeholder.
- [x] Add empty-results state.
- [x] Add network/API error state.

**Covers:** FR-SEARCH.

## Phase 8 — Movie detail UI

- [x] Clicking a search result opens detail view.
- [x] Fetch/render movie details asynchronously.
- [x] Add Back action.
- [x] Add an untracked movie to Watchlist from its detail view.
- [x] Show Watchlist/Watched state and handle save failures in the detail view.
- [x] Add contextual Watchlist/Watched actions.

**Covers:** FR-DETAIL-01, FR-DETAIL-04..05.

## Phase 9 — Watchlist UI

- [x] Render persisted Watchlist.
- [x] Clicking a Watchlist item opens detail view.
- [x] Add remove action.
- [x] Refresh view immediately after mutation.

**Covers:** FR-WATCHLIST, FR-DETAIL-02.

## Phase 10 — Watched UI

- [x] Render persisted Watched movies.
- [x] Clicking a Watched item opens detail view.
- [x] Marking watched from Watchlist moves it between sections immediately.
- [x] Add remove action.

**Covers:** FR-WATCHED, FR-DETAIL-03.

## Phase 11 — Personal rating (optional MVP+)

- [x] Add integer 1–10 rating UI.
- [x] Validate range.
- [x] Persist/update/clear rating.
- [x] Add tests.

**Covers:** FR-RATING.

## Phase 12 — Quality pass

- [ ] Review code against SE-EDU Java coding standard.
- [ ] Add Checkstyle if practical/required.
- [ ] Add Java assertions only for genuine internal assumptions.
- [x] Improve exception messages and edge cases.
- [ ] Ensure no blocking network work runs on JavaFX thread.
- [ ] Ensure no token appears in Git history/current tracked files.
- [ ] Run all unit tests.
- [ ] Manually test primary flow.

## Phase 13 — Cross-platform release

- [ ] Confirm Java/JavaFX packaging strategy supports Windows.
- [ ] Smoke-test on Windows.
- [ ] Smoke-test on Linux.
- [ ] Smoke-test on macOS.
- [ ] Build the latest distributable JAR with required libraries.
- [ ] Place the submission JAR in `release/`.
- [ ] Verify the JAR starts from a clean folder/environment as required by the assignment.
- [ ] Confirm branch is `master` before grading/submission.

> JavaFX contains platform-specific native components. Do not assume a JAR produced and tested on only one OS is automatically cross-platform. Explicitly verify the final packaging approach and test on all three required operating systems.

## Phase 14 — Documentation and final checks

- [ ] Update root `README.md` with setup/run instructions.
- [ ] Update `docs/README.md` as the user guide if required by the MP1 workflow.
- [ ] Include TMDB attribution required by current TMDB terms.
- [ ] Add screenshots if useful.
- [ ] Keep a short implementation summary/changelog for MP1.
- [ ] Run `gradlew clean test`.
- [ ] Build release artifact from a clean checkout if possible.
- [ ] Check `git status` is clean.
- [ ] Push `master`.
