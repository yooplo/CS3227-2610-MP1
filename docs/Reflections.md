# AI-Assisted Development Reflections

## Scope and evidence

These reflections are based on the Movie Tracker prompt sequence, the resulting source and tests, and the repository commits from `097debf` through `c73e6d7`. They are not intended to imply that every AI suggestion was correct or that automated checks replaced manual review. Where a manual GUI or live-TMDB check is still needed, I say so explicitly.

## Example 1: Designing the service boundary before writing TMDB code

### What I was trying to achieve

I wanted the rest of Movie Tracker to request movie searches and details without depending on TMDB JSON fields, HTTP code, or credentials. This mattered because the application-level workflow should remain stable even if the external provider or its response format changed.

### How and why I formulated the prompt

I asked for a `MovieApiService` with search and detail operations, application-level error handling, and a fake in-memory implementation. I explicitly prohibited real HTTP requests, TMDB DTOs, credentials, GUI changes, and persistence. Those negative constraints were deliberate: they forced the design decision to be evaluated independently from the convenience of whatever shape the TMDB response happened to have.

### Assumptions made by the AI

The AI assumed that search should return `List<MovieInfo>`, that an empty search result was normal, that details should return one `MovieInfo`, and that failures should use one checked `MovieServiceException` with a small `FailureType` enum. It also assumed synchronous interface methods were acceptable because JavaFX threading would be handled by the caller later. Pagination and cancellation were intentionally outside the MVP contract.

### What the AI did well

The resulting boundary kept TMDB-specific parsing inside the service package. `MovieInfo` represents externally sourced metadata, while `Movie` adds user-owned collection state through `WatchStatus`. The fake service allowed contract tests without a token or internet connection. This separation also made the later JavaFX controllers depend on `MovieApiService`, not `TmdbMovieApiService` internals.

### What was wrong or still risky

The synchronous interface does not express cancellation or asynchronous execution. That was acceptable for the MVP, but it placed responsibility on every GUI caller to avoid the JavaFX Application Thread. The original application-level validation also checked only ID and title; invalid external ratings could still enter `MovieInfo` until the hardening review.

### How I verified it

Repository commit `bd37d28` contains `MovieInfo`, `Movie`, `MovieApiService`, `MovieServiceException`, the fake service, and their tests. Commit `8202bdc` then adds the TMDB implementation without changing the interface or exposing TMDB response classes. The automated tests use predefined data and local JSON rather than live TMDB access.

### Engineering judgement I still had to apply

I had to decide that one exception plus a failure-type enum was sufficient and that a larger exception hierarchy would not improve the MVP. I also had to keep the distinction between external metadata and saved state: a search result is not automatically a user-owned movie, and conversion to `Movie` happens only when the user adds it to the collection.

### How the approach evolved

The sequence moved from an abstract service and fake, to a real TMDB adapter, and only then to JavaFX background tasks. That staged approach made each boundary testable before it gained network or UI concerns.

### What I would do differently next time

I would document interface guarantees such as non-null return values, immutable result lists, and caller-managed threading directly alongside the interface. I would also include the 0–10 external-rating invariant in the initial application-level model instead of discovering that gap during hardening.

## Example 2: Corruption-safe persistence instead of silently resetting data

### What I was trying to achieve

The persistence increment needed to complete the MVP restart workflow while protecting user-owned data. The difficult case was not ordinary serialization; it was deciding what should happen when `data/movies.json` exists but cannot be trusted.

### How and why I formulated the prompt

I specified Jackson, the relative path `data/movies.json`, automatic loading and saving, temporary test directories, and all fields needed to reconstruct a `Movie`. Most importantly, I explicitly said not to overwrite corrupted data automatically, to preserve the existing file, and to keep the application usable in a safe state. This turned corruption behavior into a design requirement instead of an afterthought.

### Assumptions made by the AI

The AI assumed that a missing file means a valid empty collection, while a present but invalid file is a different state. It chose to start an in-memory empty collection after a corrupted load and disable persistence for the rest of that run. It also assumed that a failed save should leave the successful in-memory domain mutation visible for that session and that writing a temporary file before replacement was preferable to writing directly to `movies.json`.

### What the AI did well

`MovieStorage` owns Jackson and file I/O, while `MovieCollection` remains the domain owner. `MovieCollectionManager` coordinates successful collection mutations with saves without putting file operations in `MainController`. Missing storage is handled normally, statuses and optional metadata round-trip, and corrupted-load mode prevents later clicks from replacing the original file.

### What was wrong or still risky

The first persistence version caught `IOException` and `IllegalArgumentException` during load, but a missing stored `watchStatus` caused `Objects.requireNonNull` to throw an uncaught `NullPointerException`. Jackson scalar coercion could also accept values such as a numeric ID encoded as a string. Both gaps were found in the later hardening review. Saves also remain synchronous on the JavaFX thread; this is reasonable for a small MVP collection but could cause a brief pause on a slow filesystem.

### How I verified it

Commit `658922a` added storage and manager tests using JUnit temporary directories, including missing paths, empty and multi-movie saves, both statuses, nullable metadata, round trips, malformed JSON, duplicate IDs, failed saves, and preservation when persistence is disabled. The development summary reported 65 passing tests and a successful clean build at that stage. Commit `c73e6d7` added the missing-status, unsupported-version, and scalar-type tests; the final hardening run reported 73 passing tests and a successful clean build.

### Engineering judgement I still had to apply

I had to choose data preservation over transparent recovery. Automatically replacing a corrupted file with an empty collection would make the application look healthy while destroying the only copy of the user's data. Disabling saves is less convenient, but it makes the failure visible and recoverable.

### How the approach evolved

The initial design focused on safe load/save coordination and atomic replacement. Hardening then treated stored JSON as untrusted input, broadened failure conversion to runtime mapping errors, disabled scalar coercion, and moved rating validation into the domain so invalid objects could not produce future corrupted files.

### What I would do differently next time

I would begin with an explicit storage-validity table covering missing fields, null required fields, wrong JSON types, unknown versions, duplicate IDs, and boundary numeric values. I would also consider whether a backup-copy or user-driven recovery workflow is warranted before calling persistence complete beyond the MVP.

## Example 3: Detecting a stale asynchronous movie-details race

### What I was trying to achieve

Movie details had to load without blocking JavaFX while preserving the previous search results. The initial implementation met the obvious happy path, but the hardening review needed to examine navigation and overlapping asynchronous operations rather than only whether a single request worked.

### How and why I formulated the prompts

The details prompt explicitly required a JavaFX background request, a visible loading state, duplicate-request avoidance where appropriate, safe errors, and navigation back without repeating the search. Later, the hardening prompt specifically asked for review of `MainController`, GUI state transitions, controller complexity, and confusing UI states. This second prompt broadened the analysis from feature implementation to adversarial sequences.

### Assumptions made by the AI

The first implementation assumed that hiding the search view while details loaded was enough to prevent overlapping selections. That assumption was incomplete because the top navigation remained available: a user could return to Search, select another result, and leave two detail tasks racing.

### What the AI did well

The hardening review identified that an older request could complete after a newer request and overwrite the details page. The fix tracks the active TMDB ID and a monotonically increasing request version. Re-selecting the same loading movie does not issue another call, and handlers from older requests are ignored. This preserves navigation responsiveness without putting network calls on the JavaFX Application Thread.

### What was wrong or still risky

The race should have been considered during the original details increment. The fix prevents stale UI writes but does not cancel an older HTTP request, so some unnecessary network work may continue in the background. There is also no automated JavaFX interaction test for the exact navigation sequence.

### How I verified it

The code path was inspected during the hardening review, and commit `c73e6d7` records the request-version and duplicate-loading guards. The complete automated suite and clean build passed with 73 tests. **Manual verification still required:** deliberately use a slow connection, start loading one movie, navigate back, select another, and confirm that the first response never replaces the second movie's details.

### Engineering judgement I still had to apply

I had to choose between disabling global navigation, cancelling tasks, or allowing navigation while rejecting stale completions. Keeping navigation available best matched the responsive GUI requirement. A request-version guard was smaller and less error-prone than introducing a cancellation framework during MVP hardening.

### How the approach evolved

The approach progressed from “run each call in a JavaFX `Task`” to “also define which task is allowed to update current UI state.” This was a useful reminder that background execution solves thread blocking, not ordering.

### What I would do differently next time

I would include out-of-order completion, repeated selection, and navigation-away scenarios in the original asynchronous-feature prompt. I would also isolate request coordination behind a testable helper if the application gains more concurrent operations.

## Example 4: Treating JSON deserialization as input validation

### What I was trying to achieve

During hardening, I wanted to check whether “valid JSON” necessarily meant “valid Movie Tracker data.” The concern was that Jackson might successfully deserialize values that the application itself would never write.

### How and why I formulated the prompt

The hardening prompt called out invalid stored values, duplicate entries, corrupted storage, validation, and storage reliability. It also prohibited style-only refactoring. That encouraged targeted adversarial tests instead of reorganizing `MovieStorage` for appearance.

### Assumptions made by the AI

The initial storage implementation assumed record deserialization plus the `Movie` constructor supplied sufficient validation. The review challenged that assumption and identified Jackson's default scalar coercion as a boundary weakness. It also assumed strict rejection was safer than accepting and normalizing hand-edited or corrupted values.

### What the AI did well

The hardening change disables scalar coercion, routes runtime mapping failures through `StorageException`, and centralizes rating validation for `MovieInfo` and `Movie`. Tests cover a string-encoded ID, a missing status, an unsupported format version, invalid ratings, and the existing malformed/duplicate cases.

### What was wrong or still risky

This strictness was not designed into the original persistence increment. A stricter reader may also make future format evolution require an explicit migration rather than permissive loading. Unknown extra JSON properties are still tolerated, while known fields with wrong types are rejected; that compatibility policy should remain deliberate.

### How I verified it

All storage tests use temporary paths and do not touch `data/movies.json`. The hardening suite reported 73 tests with zero failures, errors, or skips, and `git diff --check` and the clean build passed.

### Engineering judgement I still had to apply

I had to distinguish optional absence from invalid representation. A missing release date or rating is legitimate TMDB metadata and remains `null`; a required status or wrongly typed ID is not legitimate saved state and activates corruption protection.

### How the approach evolved

Validation moved from being split between the TMDB mapper and storage DTO conversion to a shared domain invariant plus strict boundary parsing. This reduces the chance that one input route can construct an object another route would reject.

### What I would do differently next time

I would write corruption tests before the first storage implementation and consider property-based generation of wrong types and boundary values. I would also document a migration strategy before incrementing the format version.

## Example 5: Choosing not to split `MainController` during hardening

### What I was trying to achieve

By the end of the MVP, `MainController` coordinated Search, Details, Watchlist, Watched, background tasks, feedback, and dynamically created entries. I wanted to improve maintainability without turning a review increment into a risky UI rewrite.

### How and why I formulated the prompt

I asked the AI to pay special attention to `MainController`, propose an extraction before performing it, avoid refactoring merely for style, and make only changes with clear engineering benefit. This wording was meant to counter a common tendency to treat class length alone as proof that a major abstraction is necessary.

### Assumptions made by the AI

The AI assumed the single-FXML structure would remain appropriate for the MVP and that most remaining controller responsibilities were genuinely JavaFX-specific. It judged that multiple controllers would require shared navigation and dependency-lifecycle changes that were not justified by the defects found.

### What the AI did well

It explicitly proposed no class-level extraction before editing. Instead, it centralized repeated `visible`/`managed` state changes in one private helper and fixed the detail-request race locally. Service calls, storage I/O, domain mutations, detail formatting, and service-error messages were already delegated to other classes.

### What was wrong or still risky

The controller remains large and difficult to unit test. Dynamic result and collection entry construction is still mixed with navigation and task coordination. Deferring extraction is not proof that the current structure will scale to post-MVP features.

### How I verified it

The hardening diff showed a focused replacement of duplicated view-state code rather than a new controller hierarchy. The final suite and clean build passed. **Manual verification still required:** resize the window and exercise every navigation transition, especially while searches or details are loading.

### Engineering judgement I still had to apply

I had to balance architectural cleanliness against regression risk and project scope. For this increment, a small consistency helper and concurrency guard addressed real defects. A multi-controller refactor would mainly prepare for features that were explicitly out of scope.

### How the approach evolved

The initial prompt left extraction available if warranted. After inspecting the actual responsibility boundaries, the decision narrowed to an internal helper rather than a structural rewrite.

### What I would do differently next time

I would define view-controller boundaries earlier if the roadmap clearly included more screens. For the current application, I would first add JavaFX interaction tests or a testable navigation state model before attempting a larger split.

## Overall reflection

The most valuable AI assistance in Movie Tracker was not raw code generation; it was the ability to work through deliberately constrained increments and then revisit earlier assumptions during hardening. The service boundary made later TMDB and JavaFX work replaceable, while adversarial review exposed failure modes that happy-path implementation prompts missed. The project also shows why AI output still required engineering judgement: the first persistence design was broadly safe but missed a runtime exception and coercion behavior, and the first asynchronous details workflow was responsive but not race-safe.

For future work, I would formulate prompts with explicit invariants and failure tables earlier, especially for concurrency and untrusted input. I would continue using small increments with automated verification, but I would record interaction summaries immediately after each increment rather than reconstructing them near submission.
