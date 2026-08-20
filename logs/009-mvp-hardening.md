# 009 — MVP hardening and quality review

## Development goal

Review the completed MVP for correctness, validation, storage/service reliability, confusing UI state, test gaps, controller complexity, and documentation accuracy without adding features.

## Important prompts and instructions

- Inspect all MVP flows and order issues by severity.
- Pay special attention to `MainController`, but propose any extraction before performing it.
- Avoid style-only refactoring.
- Review missing/corrupt storage, save failures, duplicate and invalid values, all TMDB failure categories, and navigation transitions.
- Add only high-value tests and keep them offline.
- Run the full suite, clean build, and `git diff --check`.

## AI findings and implementation

The review found two significant correctness issues: a missing stored status could cause an uncaught `NullPointerException`, and overlapping detail requests could update the UI out of order. It also found that Jackson scalar coercion accepted incorrectly typed fields, domain objects accepted invalid external ratings, null TMDB response bodies were not explicitly mapped, HTTP 408/504 were generic errors, and an already-Watched transition caused an unnecessary save.

The implementation broadened storage mapping failures into `StorageException`, disabled scalar coercion, added shared `MovieValidation`, guarded detail completion by request version, classified timeout statuses, and centralized view visibility. The README was rewritten to describe the actual MVP.

## Decisions accepted or rejected

- Accepted small, defect-driven changes and additional boundary tests.
- Rejected splitting `MainController` into multiple controllers during hardening. The controller is large, but a split would require broader FXML/navigation changes; a private view-state helper addressed the immediate duplication.
- Left synchronous persistence unchanged because the MVP data set is small.
- Left live network and JavaFX interaction tests manual rather than adding new frameworks or transport abstractions solely for coverage.

## Verification performed

The final recorded results were 73 tests, zero failures/errors/skips, a successful `clean build`, and a passing `git diff --check`. Commit `c73e6d7` records the hardening changes. Git emitted informational LF-to-CRLF working-copy warnings on Windows.

## Notable issues and lessons

The most valuable hardening cases were behaviors that happy-path tests did not exercise: unchecked constructor exceptions crossing a storage boundary, library coercion defaults, and out-of-order asynchronous completion. The decision not to perform a large refactor was also an engineering outcome, not a failure to notice controller size.

## Manual verification before submission

- Exercise the stale-detail race on a slow connection.
- Run a complete live-TMDB GUI smoke test.
- Verify all persistence restart and corruption workflows.
- Review the updated README, Developer Guide, reflections, and these reconstructed summaries for accuracy in your own development experience.
