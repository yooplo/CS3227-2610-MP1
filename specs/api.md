# Movie Tracker — TMDB API Integration Specification

## 1. Scope

For the MP1 MVP, TMDB is a read-only metadata provider. Local Watchlist/Watched state is stored by Movie Tracker itself.

Do not implement TMDB account authentication or remote watchlist synchronization unless the product spec is deliberately expanded.

## 2. API version and base URL

Use TMDB API v3 endpoints over HTTPS.

```text
https://api.themoviedb.org/3
```

## 3. Authentication

Preferred authentication for this project:

```http
Authorization: Bearer <TMDB_API_READ_ACCESS_TOKEN>
Accept: application/json
```

The token must be injected at runtime and must not be committed to the repository.

Recommended local development options, in order:

1. Environment variable `TMDB_API_TOKEN`.
2. A local untracked `.env`/properties file if necessary.

Example environment variable:

```text
TMDB_API_TOKEN=your_read_access_token_here
```

`.env` and any local secret file must be listed in `.gitignore`.

## 4. Required endpoints

### Search movies

```http
GET /search/movie?query={query}&include_adult=false&language=en-US&page=1
```

Purpose: primary search UI.

Minimum fields consumed:

- `id`
- `title`
- `release_date`
- `poster_path`
- `overview`
- `vote_average`

### Movie details

```http
GET /movie/{movie_id}?language=en-US
```

Purpose: movie detail screen.

Minimum fields consumed:

- `id`
- `title`
- `release_date`
- `runtime`
- `genres`
- `overview`
- `poster_path`
- `backdrop_path`
- `vote_average`

## 5. Optional API optimization

TMDB detail endpoints support `append_to_response`. If later features require related subresources, prefer a single detail call such as:

```text
/movie/{movie_id}?append_to_response=credits,videos
```

Only add this when the UI actually needs the data.

## 6. Posters/images

TMDB returns image paths rather than a complete URL.

For the MVP, use a documented image size such as `w342` or `w500` for posters, and construct the URL from TMDB image configuration/base information.

A typical final URL shape is:

```text
https://image.tmdb.org/t/p/w500/{poster_path}
```

However, code should ideally isolate this construction in `TmdbConfig`/`TmdbClient` rather than scattering the base URL across controllers.

If `poster_path` is null, show a local placeholder instead of treating it as an error.

## 7. HTTP client

Use Java's standard `java.net.http.HttpClient` unless a strong reason emerges to add another HTTP library.

Benefits for MP1:

- no extra HTTP dependency
- works cleanly with Java 25
- supports asynchronous requests

## 8. JSON parsing

Use one JSON library consistently, e.g. Jackson or Gson.

Recommended: Jackson databind, because DTO mapping is explicit and easy to test.

Do not pass raw JSON objects into the UI. Map TMDB payloads into DTOs and then into app domain models.

## 9. Error handling

The client must handle at least:

- missing token
- HTTP 401/403 authentication/permission errors
- HTTP 404 for unavailable movie details
- HTTP 429/rate limiting
- HTTP 5xx server failures
- timeouts / DNS / no network
- malformed/unexpected JSON

The UI should receive a domain-level error and display a concise message such as:

```text
Couldn't reach TMDB. Check your connection and try again.
```

Do not show tokens, stack traces, or raw response bodies to normal users.

## 10. Timeouts

Configure finite request/connect timeouts. A network problem must not leave the UI permanently blocked.

Suggested starting point:

- connect timeout: 5–10 seconds
- request timeout: 10–15 seconds

These values are implementation defaults, not product requirements, and may be tuned.

## 11. Caching

No complex cache is required for MP1.

Permitted lightweight behavior:

- keep search results in memory for the current session
- keep local tracked-movie snapshots for Watchlist/Watched rendering
- optionally avoid re-fetching the same detail response repeatedly during one session

## 12. TMDB attribution

Before final release, verify and include the attribution/branding wording required by TMDB's current terms of use. Do not invent attribution text in the application; check the current TMDB requirements at release time.

## 13. Test approach

Unit tests for `TmdbClient` mapping should use saved JSON fixtures or an injectable/fake HTTP layer. Automated tests must not rely on a real API token or live TMDB availability.
