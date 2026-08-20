# 003 — Core domain model

## Development goal

Represent saved movies and implement the in-memory collection behavior needed by the MVP, without GUI, network, or persistence work.

## Important prompts and instructions

- Create `Movie` with only current MVP metadata and `WatchStatus` with `WATCHLIST` and `WATCHED`.
- Use TMDB movie ID as unique identity.
- Support add, duplicate prevention, removal, lookup, status filtering, mark-as-watched, and case-insensitive local title search.
- Decide equality consistently with TMDB identity and add JUnit tests for important and edge behavior.

## AI implementation

`Movie` stores immutable metadata and a mutable watch status. Equality and hash code use only the TMDB ID, so changing status does not break hash-based identity. `MovieCollection` uses a `LinkedHashMap<Integer, Movie>` to preserve insertion order and enforce uniqueness efficiently. Tests cover equality, duplicate protection, removal, lookup, filters, status changes, title search, null handling, and unmodifiable result lists.

`MovieInfo` was also introduced as application-level external metadata. The repository commit `bd37d28` groups the domain work with the following service-abstraction work even though they were prompted as separate increments.

## Decisions accepted or rejected

- Accepted ID-only equality because the project explicitly defined TMDB ID as unique identity.
- Accepted `LocalDate` rather than storing only a release year, preserving available API detail.
- Kept status mutation inside the domain instead of allowing the controller to assign enum fields directly.
- Rejected extra abstractions for ratings, notes, genres, or discovery metadata.

## Verification performed

Domain tests were added with the implementation and remain part of the final 73-test hardening suite. The hardening increment later added rating validation and verified that marking an already-Watched movie is a no-change operation.

## Notable issues and lessons

ID-only equality is stable across metadata updates and status changes, but it also means two objects with the same ID compare equal even if their titles differ. That is intentional identity semantics, not a full state comparison.

## Manual verification before submission

- Confirm that the equality rationale matches the explanation you personally accepted during the domain-model review.
