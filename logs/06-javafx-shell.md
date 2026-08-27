# Development Log 06 — JavaFX Shell and Navigation

## Objective

Create a compact, resizable, single-window JavaFX shell with Search, Watchlist, and Watched sections, while leaving feature wiring for later increments.

## Prompt constraints and approach

The shell prompt prohibited TMDB calls, storage actions, collection population, and tracking mutations. It required clear navigation, placeholders, moderate resize support, and concrete dependency creation outside feature views. The design did not mandate FXML, so the AI selected programmatic JavaFX to avoid loader/controller overhead for a small application.

`MovieTrackerApp` became the composition root and `MainWindow` the owner of the sidebar and center-content switching. Initial structure and CSS established minimum sizing, active navigation styling, and compact desktop spacing.

## Assumptions, issues, and corrections

- Programmatic views were assumed to be the simpler maintainable choice at this scale; later screens followed the same pattern for consistency.
- The shell initially contained placeholders by design. It did not attempt speculative reusable controllers before Search and collection behavior existed.
- Visual and resize behavior could not be established by ordinary unit tests. Manual inspection remained a required verification boundary.

## Verification and human judgement

The requested verification included all tests, a full build, whitespace checking, application launch, navigation changes, and resizing. Git records the shell and CSS but not screenshots or a durable GUI test transcript. Human judgement was needed for layout proportions, whether programmatic JavaFX remained manageable, and what counted as acceptable behavior at 720×480 through 1200×800.

## Resulting commit

- `e779ea7` — `Add JavaFX application shell`
