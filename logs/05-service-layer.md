# Development Log 05 — Tracking Service and Application Coordinator

## Objective

Implement local tracking rules with persistence consistency, then expose one application-facing API for future JavaFX views.

## Prompt constraints and approach

The service prompt required injected `Storage`, TMDB-ID duplicate detection, immutable returned collections, deterministic order, clear no-op behavior, and persistence after every successful mutation. Most importantly, a failed save must not leave in-memory state ahead of the stored state. A separate prompt then requested the smallest coordinator combining `TmdbClient` and `MovieTrackerService`, without duplicating either layer's rules.

The AI implemented `MovieTrackerService` around immutable snapshot lists. Each mutation creates a proposed list, calls `Storage.save`, and publishes that list only after save success. `MovieTrackerApplicationService` delegates lookup and tracking operations while allowing `TmdbException` and `StorageException` to cross the boundary intact.

## Assumptions, issues, and corrections

- Duplicate add, already-watched transitions, and removal of untracked IDs are no-ops and do not save.
- Untracked movies may transition directly to Watched because the requirements permit marking a movie watched without first requiring Watchlist.
- A thin coordinator was preferred over a JavaFX-named controller so the application API remained independent of UI threading and controls.
- The Phase 2 mutation-persistence checkbox became genuinely complete only after this layer guaranteed saves for every successful local mutation.

## Verification and human judgement

Fake-storage tests cover initial load, filters, duplicate prevention, every transition, ratings, removals, immutable snapshots, save counts, no-save no-ops, and save-failure rollback. Coordinator tests verify delegation and exception propagation without filesystem or TMDB access. Human judgement was central to the persist-before-publish policy and the decision not to wrap already-structured exceptions in another generic service exception.

## Resulting commits

- `1160459` — `Add movie tracking service`
- `b509d09` — `Add application coordination service`
