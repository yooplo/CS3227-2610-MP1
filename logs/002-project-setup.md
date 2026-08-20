# 002 — JavaFX project setup

## Development goal

Create the smallest runnable JavaFX application and Gradle project without adding movie functionality.

## Important prompts and instructions

- Work on `master` and add `SPEC.md` to version control.
- Configure Java 21, Gradle Wrapper, JavaFX, JUnit 5, and Jackson.
- Use FXML with a controller and add a launcher if required.
- Create the required source, documentation, and log directories.
- Open a resizable window titled “Movie Tracker” with only a matching heading.
- Do not commit or push during the implementation interaction.

## AI implementation

The implementation added Gradle configuration and wrapper files, JavaFX/FXML startup classes, `Launcher`, the initial `MainController`, `MainView.fxml`, `.gitignore`, documentation placeholders, and required directories. Jackson was configured but not used. Commit `097debf` records the setup.

## Decisions accepted or rejected

- Accepted dependency injection through the FXML loader/controller factory as later features arrived.
- Kept the initial UI intentionally empty rather than anticipating movie screens.
- No movie models, API calls, or persistence were added in this increment.

## Verification performed

The repository contains the wrapper and build configuration, and all later clean builds validate that the initial Java 21/JavaFX setup remained functional.

## Notable issues and lessons

The `Launcher` entry point avoids packaging problems that can occur when the main class directly extends JavaFX `Application`. Adding Jackson early avoided a build-file change during persistence, while obeying the instruction not to use it prematurely.

## Manual verification before submission

- Historical setup-time launch output is not retained in Git; confirm whether the initial window was visually checked at that time if this detail is discussed in the final reflection.
