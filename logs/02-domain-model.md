# Development Log 02 — Domain Model

## Objective

Create a Java-only domain model for lightweight movies, detailed movie information, and locally tracked state.

## Prompt constraints and approach

The prompts required TMDB ID to be the external identity, immutable models where practical, genuine validation only, and no JavaFX, HTTP, JSON, or storage dependencies. Fields were limited to those justified by the specifications. `WatchStatus` represented the mutually exclusive `WATCHLIST` and `WATCHED` states, and personal ratings were restricted to watched movies.

The AI implemented `Movie`, `TrackedMovie`, and `WatchStatus` first. A follow-up noticed that Phase 1 still named `MovieDetails`, so that class and its tests were completed before storage work continued. `MovieDetails` uses composition with `Movie` instead of inheritance; this keeps one lightweight movie snapshot while avoiding an artificial subtype relationship.

## Assumptions, issues, and corrections

- Phase 1 was initially incomplete because `MovieDetails` remained unchecked. The follow-up prompt explicitly stopped progression to storage until it was implemented.
- Equality for `Movie`, `TrackedMovie`, and `MovieDetails` was based on TMDB ID rather than every mutable snapshot field. This was a design decision, not a default record-style equality choice.
- Optional metadata used nullable constructor inputs exposed through `Optional` or `OptionalInt`, while genres were defensively copied.

## Verification and human judgement

JUnit tests covered valid construction, boundary validation, optional values, defensive collection handling, and ID-based equality/hash codes. The full Gradle build and whitespace check were requested after the increment. Human review was needed to decide that composition was clearer than inheritance and that timestamps suggested by the specification were optional rather than required for the MVP.

## Resulting commit

- `4d82859` — `Add movie domain model`
