# Movie Tracker

Movie Tracker is a Java desktop application for CS3227 Mini Project 1. It retrieves movie information from TMDB and lets a single user maintain a persistent personal Watchlist and Watched collection.

## Current MVP

- Search TMDB by movie title or keyword.
- View a movie's title, release date, overview, and external TMDB rating where available.
- Add a movie to Watchlist without creating duplicate TMDB IDs.
- Remove a movie from Watchlist.
- Mark a Watchlist movie as Watched.
- View separate Watchlist and Watched collections.
- Restore saved movies and statuses from `data/movies.json` after restarting.
- Display understandable API, configuration, and storage errors.

Personal ratings, notes, discovery, sorting, pagination, poster downloading, and moving Watched movies back to Watchlist are not implemented in the MVP.

## Prerequisites

- Java 21, or a Gradle-supported JDK that can provision the configured Java 21 toolchain
- Internet access for dependency downloads and TMDB searches
- A TMDB API Read Access Token in the `TMDB_API_TOKEN` environment variable

In PowerShell, set the token for the current terminal session:

```powershell
$secureToken = Read-Host "TMDB API Read Access Token" -AsSecureString
$env:TMDB_API_TOKEN = [System.Net.NetworkCredential]::new("", $secureToken).Password
```

Do not place the token in source files or commit it to Git.

## Running

Windows:

```powershell
.\gradlew run
```

macOS or Linux:

```bash
./gradlew run
```

The application creates `data/movies.json` after the first collection change. See [the User Guide](docs/UserGuide.md) for workflows, peer-testing instructions, and storage recovery behavior.

## Building and testing

Windows:

```powershell
.\gradlew clean build
.\gradlew test
```

macOS or Linux:

```bash
./gradlew clean build
./gradlew test
```

Automated tests do not require a TMDB token or live internet access.

## Project structure

```text
CS3227-2610-MP1/
├── src/main/java/       Application source
├── src/main/resources/  JavaFX FXML
├── src/test/java/       JUnit tests
├── data/                Runtime data (Git-ignored)
├── docs/                User, developer, and reflection documents
├── logs/                AI interaction summaries
├── build.gradle
├── README.md
└── SPEC.md
```

`SPEC.md` is the source of truth for project requirements. The submission-ready branch is `master`.

## Documentation

- [User Guide](docs/UserGuide.md)
- [Developer Guide](docs/DeveloperGuide.md)
- [AI-Assisted Development Reflections](docs/Reflections.md)

## Acknowledgements

Movie metadata is provided by [The Movie Database (TMDB)](https://www.themoviedb.org/). This product is not endorsed or certified by TMDB. Additional dependency acknowledgements are recorded in the Developer Guide.
