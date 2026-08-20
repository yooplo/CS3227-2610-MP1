# Movie Tracker User Guide

Movie Tracker is still under development. The current application supports searching TMDB, viewing movie details, and maintaining persistent Watchlist and Watched collections.

Watchlist and Watched contents are saved locally and restored when Movie Tracker starts again. Moving a watched movie back to the Watchlist is not currently supported.

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

Saved movie data is stored relative to the directory from which the application is run at `data/movies.json`. The directory and file are created automatically after the first successful collection change; no manual setup is needed.

If existing saved data is corrupted or invalid, Movie Tracker preserves that file, starts with a safe empty in-memory collection, and displays a warning. Changes made during that run are not saved, preventing accidental replacement of the corrupted file. Keep a copy of the file before repairing or removing it manually.

## Current peer-testing scope

The current search workflow is:

1. Enter a movie title or keyword.
2. Select **Search** or press Enter.
3. Confirm that a loading indicator appears while the interface remains responsive.
4. Confirm that results show a poster, title and, where available, a release date and TMDB rating. Movies without an available poster use a clean `POSTER` placeholder.
5. Select a result to load its detailed information.
6. Confirm that the details view shows a larger poster, title, release date, overview, and TMDB rating, using clear fallbacks for missing metadata.
7. Select **Back to search results** and confirm that the prior query and results are preserved.

The current watchlist workflow is:

1. Open a movie's details.
2. Select **Add to Watchlist**.
3. Confirm that the application reports a successful addition and disables the action.
4. Select **Watchlist** in the top navigation.
5. Confirm that the movie is listed with its poster thumbnail, available release date, and TMDB rating.
6. Select **Remove** beside that movie to remove only that entry.
7. Return to **Search**; the previous query and results remain available without another search.

The current watched-movies workflow is:

1. Open **Watchlist**.
2. Select **Mark as Watched** beside a movie.
3. Confirm that it disappears from Watchlist immediately.
4. Open **Watched** and confirm that the movie appears with its poster thumbnail, available release date, and TMDB rating.
5. Search for and open that movie again; confirm that the details view says it is already watched and does not allow it to be added to Watchlist again.

To verify persistence, close and restart Movie Tracker after adding a movie or marking it as watched. The movie should return in the same view and with the same status. Removing a Watchlist movie is also saved immediately.

Also verify that:

- A blank query displays guidance without sending a request.
- A query with no matches displays a clear message.
- A missing or invalid token displays configuration guidance.
- Network and service failures display understandable messages without stack traces.
- A details request failure displays an understandable message and still allows returning to the search results.
- Opening a movie that is already saved reports that it is already in the collection and does not create a duplicate.
- Removing a movie allows it to be added again after opening its details later.
- A watched movie does not appear in Watchlist and cannot be moved back in this version.
- A missing or unavailable poster keeps the `POSTER` placeholder and does not break the movie card.
