# CS3227-2610-MP1 Movie Tracker

A Java desktop movie tracking application powered by the TMDB API.

## Module

CS3227 Mini Project 1 — AI-assisted Software Engineering

## Status

The core Movie Tracker functionality is implemented, including TMDB search and
details, local Watchlist/Watched collections, and personal ratings.

## Prerequisite

- JDK 25

Verify the active JDK:

```bash
java --version
javac --version
```

Both commands should report Java 25. A system Gradle installation is not
required because the repository includes the Gradle Wrapper.

## Build

macOS/Linux:

```bash
./gradlew build
```

Windows:

```powershell
.\gradlew.bat build
```

## Run

macOS/Linux:

```bash
./gradlew run
```

Windows:

```powershell
.\gradlew.bat run
```

Set `TMDB_API_TOKEN` in the runtime environment to enable TMDB search. The
application still opens without it and shows a configuration message when a
search is attempted. Never place the token in a tracked file.

## Test

macOS/Linux:

```bash
./gradlew test
```

Windows:

```powershell
.\gradlew.bat test
```

## Release JAR

Build the executable submission JAR for the current operating system and CPU:

```bash
./gradlew releaseJar
```

Windows:

```powershell
.\gradlew.bat releaseJar
```

The task replaces existing `release/*.jar` files and writes:

```text
release/MovieTracker.jar
```

Run it from the directory where local Movie Tracker data should be stored:

```bash
java -jar MovieTracker.jar
```

The JAR includes Jackson and the JavaFX runtime libraries/native binaries for
the selected target. JavaFX natives are platform- and architecture-specific, so
the JAR is not universal. Build on the target system, or explicitly select a
supported OpenJFX target such as:

```bash
./gradlew releaseJar -PjavafxPlatform=linux
./gradlew releaseJar -PjavafxPlatform=linux-aarch64
./gradlew releaseJar -PjavafxPlatform=mac
./gradlew releaseJar -PjavafxPlatform=mac-aarch64
./gradlew.bat releaseJar -PjavafxPlatform=win
```

Each target must be smoke-tested on its matching operating system and CPU before
release. The committed `MovieTracker.jar` is the artifact built and tested for
the submission environment; rebuilding it replaces that artifact.
