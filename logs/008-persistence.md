# 008 — Local JSON persistence

## Development goal

Complete the MVP restart workflow by loading and saving the existing movie collection at `data/movies.json`, while preserving corrupted data and keeping the application usable after failures.

## Important prompts and instructions

- Serialize every field required to reconstruct a saved `Movie` with Jackson.
- Load automatically at startup and save after add, remove, and mark-as-watched operations.
- Treat a missing directory/file as a normal first launch.
- Preserve corrupted JSON and prevent accidental overwrite after a failed load.
- Show understandable save/load feedback without exposing filesystem details.
- Keep file I/O out of `MainController` and `MovieCollection`.
- Use temporary directories for tests and never access TMDB.

## AI recommendations and implementation

The AI proposed `MovieStorage` for JSON/file responsibilities and `MovieCollectionManager` to coordinate domain mutations with persistence. `MovieTrackerApplication` loads the collection once and injects the manager. A missing file produces an empty persistence-enabled collection. A corrupted file produces an empty in-memory collection with persistence disabled for the run. Saves write a temporary file in the target directory before replacing `movies.json`, using an atomic move where supported.

## Decisions accepted or rejected

- Accepted preservation over automatic reset for corrupted data.
- Accepted session-only continuation after load or save failure, with visible feedback.
- Accepted full-collection snapshots instead of incremental records because the MVP is a small single-user application.
- Rejected Jackson annotations/types in the domain layer.
- Rejected automatic retries and recovery writes.

## Verification performed

Commit `658922a` contains storage and manager tests for empty/multiple saves, status preservation, loading, round trips, missing paths, nullable metadata, malformed/invalid JSON, duplicates, failed saves, and disabled-persistence protection. The recorded implementation report states that 65 tests and the clean build passed. The application launch was confirmed at process level and then stopped; it did not create `data/` without a mutation.

## Notable issues and lessons

The corruption strategy successfully prevented data loss, but the first validation boundary was incomplete. A missing status could escape as an unchecked exception, and Jackson could coerce a string ID to an integer. The hardening increment corrected both. Synchronous snapshot saves are simple and consistent but may not scale to a large collection.

## Manual verification before submission

- Repeat first launch, add/restart, mark/restart, and remove/restart workflows visually.
- Back up `data/movies.json`, corrupt it deliberately, and confirm the warning and no-overwrite behavior.
- Confirm filesystem permission failures produce safe feedback on the target operating system.
