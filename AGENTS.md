# AGENTS.md — Movie Tracker MP1

## Project

Build a Java desktop movie tracker using JavaFX and the TMDB API.

The spec files in `specs/` are the source of truth for implementation decisions.

## Mandatory project constraints

- Use Java SE 25 by default.
- This is a Java desktop application.
- Use JavaFX for the GUI.
- The app must function on Windows, Linux, and macOS.
- Keep Java source under `src/main/java`.
- Keep tests under `src/test/java`.
- Use Gradle and the Gradle Wrapper.
- The grading branch must be named `master`.
- The latest submission JAR belongs under `release/` and must include required libraries according to MP1 instructions.
- Never commit TMDB API credentials.

## Spec-driven workflow

Before changing code:

1. Read `specs/requirements.md`.
2. Read the relevant sections of `specs/design.md` and `specs/api.md`.
3. Locate the implementation step in `specs/tasks.md`.
4. If the requested behavior conflicts with a spec, update the spec first or explicitly ask for a decision.

After each code change:

1. Update/add relevant JUnit tests.
2. Run the smallest relevant test set, then the full test suite when appropriate.
3. Check that no secrets are present in tracked files.
4. Summarize what changed, which requirement IDs were addressed, tests run, and any remaining limitation.

## Coding approach

- Proceed in small, reviewable increments.
- Prefer simple OOP boundaries over unnecessary abstraction.
- Keep JavaFX controllers focused on UI behavior.
- Keep domain models independent of JavaFX.
- Keep TMDB HTTP code inside the API layer.
- Keep local file IO inside the storage layer.
- Keep business rules in the service/domain layer.
- Do not make live TMDB calls from unit tests.
- Do not block the JavaFX Application Thread with network requests.
- Use OS-independent paths.
- Handle missing local data files/directories on first run.

## Security

- Read the TMDB Read Access Token from `TMDB_API_TOKEN` or another explicitly untracked runtime configuration.
- Never hard-code the token in source, tests, README files, Gradle files, or committed properties.
- Never log the full token.

## Git

- Work on `master` unless a temporary feature branch is explicitly requested.
- Keep commits small and coherent.
- Do not commit build output except the submission artifact in `release/` when required by MP1.
- Do not rename `master` to `main`.
