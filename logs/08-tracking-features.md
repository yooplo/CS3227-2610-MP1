# Development Log 08 — Tracking Collections and Ratings

## Objective

Connect the detail and collection views to local tracking in small increments: Watchlist add/view/remove, mark Watched, Watched view/remove, and personal ratings.

## Prompt constraints and approach

Each prompt limited scope to one behavior and prohibited unrelated screens or features. Views had to call only `MovieTrackerApplicationService`; storage-backed mutations had to run on the shared background executor; UI state could change only after persistence succeeded. Details navigation had to remember whether it originated from Search, Watchlist, or Watched.

The AI added tracking controls to `MovieDetailsView`, then focused `WatchlistView` and `WatchedView` classes. `PosterView` was reused for collection rows. Marking Watched relies on service transitions, and collection screens refresh from service snapshots on navigation/return. Rating controls expose integers 1–10 only for watched movies and support update/clear. Both collection views reuse the same service removal operation rather than creating status-specific business logic.

## Assumptions, issues, and corrections

- Removal actions were placed directly in collection rows to match the existing Watchlist management pattern and make list management efficient.
- UI code displays progress and disables duplicate actions but does not replicate transition, validation, or rollback rules.
- Local file operations were moved off the JavaFX thread even though individual writes are small, keeping network and persistence interactions consistent.
- There was no brittle JavaFX automation infrastructure, so non-visual behavior was verified through service/coordinator tests and GUI flows remained manual.

## Verification and human judgement

Tests cover duplicate prevention, untracked/Watchlist/Watched transitions, mutual exclusion, ratings and boundaries, removals, persistence counts, no-op behavior, and rollback. Manual flows were requested for add, reopen, restart persistence, movement between collections, origin-aware Back, rating refresh, and removal. UX placement, progress feedback, and whether to extract shared row abstractions required human review; unnecessary abstraction was avoided.

## Resulting commits

- `a6e4b94` — `Add watchlist tracking action`
- `0b2b5fb` — `Add watchlist view`
- `369d176` — `Add mark as watched action`
- `15398da` — `Add watched movies view`
- `33d35d6` — `Add personal movie ratings`
- `c9f5873` — `Add watched movie removal`
