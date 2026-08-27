# Development Log 03 — Local JSON Storage

## Objective

Persist tracked movies to a local, versioned JSON file without coupling Jackson to the domain model.

## Prompt constraints and approach

The storage prompt required OS-independent `Path` usage, an empty result for a missing first-run file, creation of missing directories during save, temporary-directory tests, safe replacement, and a project-specific failure boundary for malformed data. It explicitly prohibited silently loading corrupted data as empty and later overwriting it.

The AI introduced `Storage`, `LocalStorage`, `StorageException`, `StorageData`, and `TrackedMovieData`. The persisted document has a top-level version and stores only the tracked snapshot needed to reconstruct `TrackedMovie`. Saving writes a temporary file in the target directory and attempts an atomic replace, with a normal replace fallback where atomic moves are unavailable.

## Assumptions, issues, and corrections

- Persistence DTOs were chosen over Jackson annotations on `Movie` and `TrackedMovie`, preserving the domain layer's independence.
- Missing storage is normal; malformed JSON, unsupported versions, invalid tracked data, and I/O failures are not.
- The storage layer alone could not satisfy “save after every tracking mutation.” That Phase 2 checkbox was deferred until `MovieTrackerService` coordinated every successful mutation.

## Verification and human judgement

Tests used JUnit temporary directories and covered missing files, directory creation, round trips, multiple statuses, optional ratings, malformed/empty data, invalid domain data, and version failures. No test wrote to the repository's real `data/` directory. Human judgement was required to choose whole-state replacement and a versioned DTO format rather than incremental writes or annotations on domain classes.

## Resulting commit

- `1d6ea67` — `Improve TMDB error handling`

This commit also contains the TMDB API work. Git does not provide a separate storage commit even though storage was developed as its own prompt increment.
