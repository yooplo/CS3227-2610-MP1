# Movie Tracker — Spec-Driven Development Guide

This folder is the source of truth for the Movie Tracker MP1 implementation.

## How to use these specs

Before implementing a feature:

1. Read `requirements.md` and identify the requirement IDs being implemented.
2. Read `design.md` and `api.md` for architecture and integration constraints.
3. Find the matching item in `tasks.md`.
4. Implement one small, reviewable increment at a time.
5. Add/update tests for the increment.
6. Run the relevant Gradle checks.
7. Update the spec first if implementation needs to deviate from it.
8. Commit only after the increment is working and understood.

## Spec precedence

If documents conflict, use this order:

1. Module/MP1 instructions supplied by the teaching team.
2. `requirements.md` — product behavior and constraints.
3. `design.md` — architecture and code organization.
4. `api.md` — TMDB-specific integration rules.
5. `tasks.md` — implementation plan.

## MP1 constraints captured here

- Java desktop application.
- Java SE 25 by default.
- JavaFX GUI.
- Must run on Windows, Linux, and macOS.
- Source code belongs under `src/`.
- Latest distributable JAR belongs under `release/`.
- Build should be automated with Gradle.
- Git branch used for grading must be named `master`.
- Summaries are sufficient for recording progress.

## Proposed product scope

Movie Tracker is a personal desktop application for discovering movies through TMDB and maintaining two local collections:

- **Watchlist** — movies the user wants to watch.
- **Watched** — movies the user has finished watching.

The MVP intentionally keeps tracking data local. TMDB is used as a read-only source of movie metadata. TMDB account login/synchronization is out of scope unless the spec is deliberately revised later.
