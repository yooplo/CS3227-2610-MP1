# Development Log 04 — TMDB Client and Error Audit

## Objective

Implement offline-testable TMDB search/details access and then audit all Phase 4 error-handling requirements before adding more code.

## Prompt constraints and approach

The API prompt required Java's standard HTTP client, Bearer authentication from `TMDB_API_TOKEN`, DTO-to-domain mapping, dependency injection for HTTP behavior, no live calls in tests, and no token or raw-response exposure. The follow-up Phase 4 prompt was intentionally conservative: inspect existing behavior, avoid duplication, and add code only for genuine gaps.

The AI introduced `HttpTransport` and `JdkHttpTransport`, `TmdbClient`, request-time configuration, Jackson DTO records, `TmdbException`, and `TmdbErrorCategory`. Search maps to `Movie`; details map to `MovieDetails`. Structured categories cover missing configuration, network failures, timeouts, HTTP errors, invalid responses, and interruption. UI message mapping was kept outside the API package.

## Assumptions, issues, and corrections

- Most Phase 4 behavior was already present by the time of the audit. The prompt prevented a second exception hierarchy or duplicate wrappers.
- Explicit timeout categorization and tests received particular scrutiny so timeout failures would not collapse into a generic network message.
- The exact prompt-level before/after state cannot be reconstructed from Git because storage, Phase 3, and Phase 4 were committed together. The current tests prove timeout categorization and cause preservation, but the log does not claim an unrecorded separate commit.
- Runtime configuration was made lazy so a missing token would fail Search/Details requests without preventing application startup.

## Verification and human judgement

Fixture-based tests cover authenticated request construction, URL encoding, search/details mapping, blank input without transport calls, null optional fields, malformed JSON, invalid domain data, HTTP errors without response-body leakage, network errors, timeouts, invalid IDs, and missing tokens. No automated test contacts TMDB. Human testing with a real token remained necessary for actual service behavior.

## Resulting commit

- `1d6ea67` — `Improve TMDB error handling`
