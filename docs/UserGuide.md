# Movie Tracker User Guide

Movie Tracker is a compact desktop application for finding movies through TMDB and keeping a local Watchlist and Watched collection.

## Before you start

- Install Java 25 and confirm that `java --version` reports version 25.
- The current `MovieTracker.jar` release is packaged for **Windows x86-64**. It is not a universal JAR; Linux and macOS artifacts have not yet been built and verified.
- A TMDB API Read Access Token is required for **Search and Movie Details only**. The application still starts without a token, and locally saved Watchlist and Watched entries remain available.

## Configure TMDB on Windows

Obtain an API Read Access Token from your own TMDB account. Never put the token in this repository or share it in screenshots.

For the current PowerShell session, replace the placeholder with your token:

```powershell
$env:TMDB_API_TOKEN = "your_tmdb_read_access_token"
```

Then run Movie Tracker from the same PowerShell session. To configure it persistently, open **Edit environment variables for your account** in Windows, add a user variable named `TMDB_API_TOKEN`, and open a new terminal afterward.

If no token is configured, Search and Details show `TMDB API token is not configured.` instead of closing the application.

## Run the release

Place `MovieTracker.jar` in the directory from which you want to run it, open a terminal in that directory, and run:

```powershell
java -jar MovieTracker.jar
```

The main window opens with Search, Watchlist, Watched, and About navigation on the left.

## Using Movie Tracker

### Search

1. Select **Search**.
2. Enter a movie title and press Enter or select **Search**.
3. Select a result to open Movie Details.

Search results show the movie title and release year when available. A blank query is rejected locally and does not contact TMDB. If there are no matches, Movie Tracker displays a no-results message. Search-result poster thumbnails are not included in the current release.

### Movie Details

The Details view loads in the same window and shows available TMDB information: title, release date, poster, overview, runtime, genres, and TMDB rating. Missing metadata and poster failures use placeholders rather than failing the whole view.

Use **Back to Search**, **Back to Watchlist**, or **Back to Watched** to return to the section from which Details was opened. Returning to Search preserves the latest query and results.

### Watchlist

- From an untracked movie's Details view, select **Add to Watchlist**.
- Select **Watchlist** to see saved Watchlist movies.
- Select a row to open its Details view.
- Select **Remove** on a Watchlist row to stop tracking it.

Adding the same TMDB movie again does not create a duplicate. The Watchlist updates after a successful save.

### Watched

- From an untracked movie or a Watchlist movie's Details view, select **Mark as Watched**.
- A Watchlist movie moved to Watched no longer appears in Watchlist.
- Select **Watched** to see saved watched movies.
- Select a row to open Details, or select **Remove** to stop tracking it.

A movie cannot be in Watchlist and Watched at the same time. Removing a watched movie also removes its personal rating.

### Personal ratings

Rating controls appear only for watched movies. In Details, choose an integer from 1 to 10 and save it. You can update an existing rating or select **Clear rating**. Saved ratings appear in the Watched list and are separate from TMDB's public rating.

### About and credits

Select **About** to view application information and TMDB attribution. This section uses no network, storage, or tracking state and remains available without `TMDB_API_TOKEN`. Movie Tracker is the CS3227 MP1 application; movie metadata and images displayed by it are supplied by TMDB. The About section includes TMDB's approved logo and required non-endorsement notice.

## Local data

Tracked movies and personal ratings are stored in:

```text
data/movies.json
```

This path is relative to the terminal's current working directory when `java -jar MovieTracker.jar` is run. If you start the JAR from its own directory, the `data` directory is created beside the JAR when the first tracking change is saved. The data file contains no TMDB token.

To retain your collections, keep or back up `data/movies.json`. Running the same JAR from a different working directory uses a different local data file.

## Errors and recovery

- **Missing token:** The window opens normally. Search and Details report that the token is not configured; local collection viewing and removal still work.
- **No search results:** A clear no-results message is shown. Try a different title.
- **Network, timeout, or TMDB API failure:** Search or Details shows a concise error and the application remains open. Check the connection and try again.
- **Tracking save failure:** The requested add, move, rating, or removal is not shown as successful. Check that the working directory and `data` folder are writable, then retry.
- **Corrupted, unsupported, or unreadable storage:** Startup shows an error and exits cleanly. The existing file is left unchanged to avoid data loss. Back up `data/movies.json`, then restore a known-valid copy or move the invalid file aside before restarting. Moving it aside starts Movie Tracker with an empty local collection; it does not repair or import the old file.

Movie Tracker does not display API tokens, authorization headers, raw TMDB responses, raw JSON, or stack traces in normal user-facing errors.
