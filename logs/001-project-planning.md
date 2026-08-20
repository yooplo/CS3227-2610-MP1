# 001 — Project planning and scope

## Development goal

Understand the CS3227 MP1 constraints, define the Movie Tracker MVP, inspect the initially empty repository, and propose a minimal Java desktop architecture and roadmap without implementing code.

## Important prompts and instructions

- Read `README.md` and `SPEC.md` first and treat `SPEC.md` as authoritative.
- Keep the project a Java desktop application and use `master` as the submission branch.
- Separate the MVP from planned or optional features.
- Propose technologies, dependencies, project structure, and small implementation increments before editing files.

## AI recommendations or output

The next recorded user instruction explicitly approved the proposed plan and fixed the major choices: Java 21, JavaFX with FXML/controllers, Gradle Wrapper, JUnit 5, Java `HttpClient`, Jackson, TMDB, JSON persistence at `data/movies.json`, TMDB ID identity, and `WATCHLIST`/`WATCHED` statuses. This confirms the planning interaction occurred, although its full response is not stored in the repository.

## Decisions accepted or rejected

- Accepted a desktop JavaFX application rather than a web application.
- Accepted a minimal dependency set and standard Java HTTP client.
- Accepted TMDB metadata plus local user-owned JSON data.
- Deferred personal ratings, notes, advanced discovery, sorting, and watched-to-watchlist transitions.
- Required corrupted data to be preserved rather than silently overwritten.

## Verification performed

Later commits implement the approved stack and MVP sequence on `master`. `SPEC.md` remained the governing project document throughout the recorded prompts.

## Notable issues and lessons

Separating planning from implementation made later prompts much more precise. Negative scope statements were important because `SPEC.md` contains desirable post-MVP capabilities that were not meant to be implemented yet.

## Manual verification before submission

- Confirm that this summary matches the original planning response, which is not present in Git history.
- Confirm the official CS3227 requirements independently, especially repository visibility and submission deadlines.
