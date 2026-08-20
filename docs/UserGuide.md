# Movie Tracker User Guide

Movie Tracker is still under development. The current application only opens a resizable desktop window displaying the heading **Movie Tracker**.

Movie search, movie details, watchlists, watched status, and local persistence have not been implemented yet.

## Prerequisites

- Java 21, or a Gradle-supported JDK that can provision the configured Java 21 toolchain
- An internet connection for the first build so Gradle can download required dependencies

## Running the application

On Windows:

```powershell
.\gradlew run
```

On macOS or Linux:

```bash
./gradlew run
```

## Current peer-testing scope

Verify that the application opens a resizable window titled **Movie Tracker** and displays the heading **Movie Tracker**. There are no other user-facing workflows in this development increment.
