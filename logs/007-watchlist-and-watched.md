# 007 — Watchlist and Watched workflows

## Development goal

Complete the in-memory collection workflow before adding persistence: save a viewed movie, remove it, mark it Watched, and navigate between Search, Watchlist, and Watched.

## Important prompts and instructions

- Use one shared `MovieCollection` for the application session.
- Convert `MovieInfo` to `Movie` once, assigning `WATCHLIST` without duplicating conversion logic.
- Prevent duplicate TMDB IDs without overwriting an existing status.
- Add Watchlist removal by the intended TMDB ID.
- Reuse `MovieCollection.markAsWatched`, keep the transition one-way, and prevent re-adding an already-Watched movie.
- Preserve existing search results and do not add persistence yet.

## AI implementation

Commit `aa27ad1` added `MovieFactory`, the Add to Watchlist details action, Watchlist navigation, entries, duplicate feedback, and removal. Commit `adc991d` added the Watched view and delegated the transition to `MovieCollection.markAsWatched`. Opening details for an already-Watched movie disables the add action and gives status-specific feedback.

## Decisions accepted or rejected

- Accepted a single collection created by `MovieTrackerApplication` and injected into the controller.
- Accepted a small `MovieFactory` as the only `MovieInfo` → `Movie` conversion point.
- Accepted immediate view refreshes after mutations.
- Rejected watched-to-watchlist transitions, collection sorting, ratings, notes, and persistence in these increments.

## Verification performed

Tests cover factory field copying, optional metadata, duplicate prevention, targeted removal, filtering, and one-way status changes. The later persistence manager retained the same domain operations rather than reimplementing them.

## Notable issues and lessons

The same TMDB ID remains the identity across Search, Watchlist, and Watched. This prevents a fresh search result from resetting a saved Watched movie back to Watchlist. Hardening later made marking an already-Watched movie explicitly report no change, avoiding an unnecessary save.

## Manual verification before submission

- Add the same movie twice and confirm only one entry exists.
- Remove a Watchlist movie, reopen its details, and confirm it can be added again.
- Mark a movie Watched and confirm it disappears from Watchlist, appears in Watched, and cannot be re-added.
- Confirm navigation does not issue an unnecessary search request.
