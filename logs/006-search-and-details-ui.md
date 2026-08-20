# 006 — Search and movie-details UI

## Development goal

Expose TMDB search and detail retrieval through a responsive JavaFX GUI while preserving search state and keeping network work off the JavaFX Application Thread.

## Important prompts and instructions

- Add query input, Search action, results, and loading/error/empty feedback.
- Use `MovieApiService`, with `TmdbMovieApiService` only at application composition.
- Run search and detail calls in JavaFX background tasks.
- Display title and optional release/rating in results, then title, date, overview, and rating in details.
- Preserve results when returning from Details, safely map service failures, and do not add posters or watchlist actions yet.

## AI implementation

Commit `4fb17ff` added search controls, background `Task<List<MovieInfo>>`, result buttons, loading state, and `MovieSearchMessages`. Commit `c7c224e` added selectable results, background detail requests, fallback formatting in `MovieDetailsText`, safe detail errors, and navigation back to the still-populated search view.

## Decisions accepted or rejected

- Accepted daemon threads around JavaFX `Task` for the small MVP rather than introducing an executor/application framework.
- Accepted programmatically generated result controls within the existing FXML screen.
- Kept TMDB HTTP/JSON code out of the controller.
- Deferred image downloading, pagination, and separate screen controllers.

## Verification performed

Non-GUI tests cover failure-message mapping and missing-metadata formatting without network access. Later clean builds verified FXML/resource compilation. The hardening review found and fixed an out-of-order detail-response race.

## Notable issues and lessons

Moving work to a background thread solved UI blocking but did not automatically solve request ordering. Because top navigation remained active, a user could return to Search and select another result before the first detail request completed. Commit `c73e6d7` added duplicate-loading and stale-completion guards.

## Manual verification before submission

- Verify Search → Details → Search preserves results without another API request.
- Verify loading indicators and resizing visually.
- On a slow connection, select two different movies in sequence and confirm an older response cannot replace the latest details.
- Confirm that no raw exception or HTTP body appears in the GUI.
