# Movie Tracker User Guide

Movie Tracker is still under development. The current application supports searching TMDB for movies by title or keyword.

Movie details, watchlists, watched status, and local persistence have not been implemented yet.

## Prerequisites

- Java 21, or a Gradle-supported JDK that can provision the configured Java 21 toolchain
- An internet connection for the first build so Gradle can download required dependencies
- A TMDB API Read Access Token provided through the `TMDB_API_TOKEN` environment variable

In PowerShell, set the token for the current terminal session without placing it directly in command history:

```powershell
$secureToken = Read-Host "TMDB API Read Access Token" -AsSecureString
$env:TMDB_API_TOKEN = [System.Net.NetworkCredential]::new("", $secureToken).Password
```

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

The current search workflow is:

1. Enter a movie title or keyword.
2. Select **Search** or press Enter.
3. Confirm that a loading indicator appears while the interface remains responsive.
4. Confirm that results show a title and, where available, a release date and TMDB rating.

Also verify that:

- A blank query displays guidance without sending a request.
- A query with no matches displays a clear message.
- A missing or invalid token displays configuration guidance.
- Network and service failures display understandable messages without stack traces.
