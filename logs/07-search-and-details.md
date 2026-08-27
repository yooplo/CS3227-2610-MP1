# Development Log 07 — Search and Movie Details

## Objective

Wire TMDB Search into the shell, then add asynchronous Search-to-Details navigation without implementing tracking actions yet.

## Prompt constraints and approach

Search had to reject blank input locally, support Enter, avoid blocking the JavaFX Application Thread, guard duplicate submissions, show progress/empty/error states, and render existing `Movie` objects. The Details follow-up required selectable results, single-window origin-aware navigation, asynchronous detail loading, state preservation when returning to Search, poster placeholders, and reuse of error-category mapping.

The AI injected `MovieTrackerApplicationService` and a shared executor from `MovieTrackerApp`. `SearchView` and `MovieDetailsView` use JavaFX `Task`; success/failure handlers run on the JavaFX thread. `MainWindow` retains the same Search view instance while Details is shown. The Search-only error helper was renamed to the reusable `TmdbErrorMessages`, and `TmdbImageUrls` centralized poster URL construction.

## Assumptions, issues, and corrections

- Search-result poster thumbnails were deliberately deferred to avoid expanding the increment; the task remains unchecked. Details posters were implemented with JavaFX background image loading and a non-fatal placeholder.
- Error messages depend on `TmdbErrorCategory`, not exception-text parsing.
- UI responsiveness and real TMDB results could not be proven by offline unit tests alone. Missing-token behavior was testable without a credential; success required manual testing with the student's runtime token.

## Verification and human judgement

Tests cover API mappings, image URL construction, and every error-category message. Manual checks were requested for blank, successful, empty, repeated, failed, and responsive searches; Details loading, Back behavior, optional metadata, and subsequent selections also required human observation. Human judgement determined that retaining Search state was more useful than reissuing the API request.

## Resulting commits

- `2c3b2a8` — `Add movie search interface`
- `d6a12de` — `Add movie details view`
