# 005 — TMDB integration

## Development goal

Implement the real TMDB adapter behind `MovieApiService` without changing the GUI or relying on live network access in automated tests.

## Important prompts and instructions

- Use Java `HttpClient`, Jackson, and `TMDB_API_TOKEN`.
- Support search and details while keeping TMDB structures internal.
- Map network, timeout, malformed response, authentication, rate limit, not found, and other HTTP failures to `MovieServiceException.FailureType`.
- Handle missing optional metadata and reject obviously invalid movie data.
- Use sensible timeouts, do not retry, and do not require a token or internet in tests.

## AI implementation

`TmdbMovieApiService` builds authenticated GET requests, applies connect/request timeouts, and classifies non-success status codes. `TmdbMovieMapper` parses JSON trees and converts them into validated `MovieInfo` objects. Search query text is URL encoded, adult results are excluded, and empty result arrays return an empty list.

## Decisions accepted or rejected

- Accepted an environment variable rather than a configuration file containing a secret.
- Accepted an internal mapper instead of public TMDB DTO classes because the response subset was small.
- Rejected automated retry behavior to avoid hidden delays and policy decisions during the MVP.
- Rejected live integration tests in favor of deterministic mapper and status tests.

## Verification performed

Commit `8202bdc` contains the service, mapper, and tests for response conversion, optional values, malformed JSON, invalid types/dates/ratings, empty results, HTTP status mapping, missing credentials, and invalid IDs. The hardening increment later added null-body validation and classified HTTP 408/504 as timeouts.

## Notable issues and lessons

The first implementation correctly separated parsing but did not explicitly handle a null successful response body; hardening converted that case to `INVALID_RESPONSE`. Low-level `HttpClient` network and timeout catch branches are not directly mocked, although their failure mappings and user-facing messages are covered separately.

A follow-up interaction asked how to complete TMDB's Developer Plan registration form. The available conversation shows the question and screenshot but not the assistant's answer, so this log does not reconstruct or endorse specific form entries.

## Manual verification before submission

- Perform a real search with a valid token and confirm current TMDB compatibility.
- Check missing/invalid token, offline network behavior, and rate limiting manually where practical.
- Verify any TMDB registration guidance against the actual answer you received and TMDB's current terms.
- Do not include a real token in logs, screenshots, or commits.
