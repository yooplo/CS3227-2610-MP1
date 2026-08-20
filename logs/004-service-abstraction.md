# 004 — Movie service abstraction

## Development goal

Define the application-facing movie search/details contract and failure model before writing any TMDB or HTTP code.

## Important prompts and instructions

- Add `MovieApiService.searchMovies(String)` and `getMovieDetails(int)`.
- Keep the interface independent of TMDB DTOs and raw JSON.
- Use a minimal application-level failure design covering network, timeout, invalid response, authentication, rate limit, and not found.
- Provide a fake implementation that returns predefined results/details and simulated failures.
- Do not add HTTP, credentials, GUI, or persistence.

## AI recommendations and implementation

The AI chose synchronous methods returning `List<MovieInfo>` and `MovieInfo`, with checked `MovieServiceException` carrying a `FailureType`. Empty search results are represented by an empty immutable list rather than an exception. `FakeMovieApiService` supports configured results, details by TMDB ID, and injected failures.

## Decisions accepted or rejected

- Accepted one exception class with an enum instead of a deep exception hierarchy.
- Accepted application-level `MovieInfo` as the service boundary type.
- Kept threading outside the interface so the same service could be used in tests and JavaFX background tasks.
- Deferred cancellation, pagination, retries, and provider-specific options.

## Verification performed

Contract and fake-service tests are recorded in commit `bd37d28`. Later TMDB and GUI commits use the interface without changing the domain model, demonstrating that the boundary was sufficient for the MVP.

## Notable issues and lessons

Designing the seam first prevented TMDB response shapes from spreading through controllers. The trade-off is that callers must know synchronous calls can block and must provide background execution when used by JavaFX.

## Manual verification before submission

- The exact original AI explanation of the method-signature trade-offs is not stored in Git; compare this summary with any retained chat export before quoting it as a transcript.
