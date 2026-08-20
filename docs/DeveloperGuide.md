# Movie Tracker Developer Guide

Movie Tracker is still under development. This document currently describes only the initial project setup; it does not claim that movie-tracking features are implemented.

## Current architecture

The application uses Java 21, Gradle, JavaFX, and FXML. `Launcher` starts `MovieTrackerApplication`, which loads `MainView.fxml`. The FXML view is associated with `MainController`.

JUnit 5 is configured for future automated tests. Jackson is configured as a dependency for future JSON processing but is not used in the current increment.

TMDB integration, domain models, watchlists, watched status, and persistence have not been implemented.

## Acknowledgements

The project currently depends on JavaFX, Jackson, JUnit 5, Gradle, and the Foojay Gradle toolchain resolver. Specific external API acknowledgements will be added when API integration is implemented.
