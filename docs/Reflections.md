# Reflections on AI-Assisted Software Engineering

## Approach

Movie Tracker was developed through a sequence of deliberately bounded prompts using OpenAI Codex. The prompts normally required the AI to read `AGENTS.md` and the specifications, implement one increment, run focused and complete checks, report limitations, and stop for review. This was more effective than asking for the entire application at once because each response could be compared against a small part of the task plan.

The repository and available interaction summaries do not preserve every raw response or terminal transcript. This reflection therefore discusses recoverable prompt objectives, committed results, and observed corrections rather than inventing exact quotations.

## Prompt example 1 — Separating planning from implementation

The initial repository prompt instructed Codex to inspect the repository, specifications, Gradle configuration, documentation, and Java installation, but explicitly prohibited file changes. Only after reviewing the proposed setup did a second prompt authorize the Java/Gradle/JavaFX foundation.

This formulation was intended to prevent the model from treating a broad project description as permission to scaffold its preferred architecture. It forced the first response to identify constraints such as Java 25, `src/main/java`, the `master` branch, JavaFX, Gradle Wrapper use, the plain `Launcher`, and the submission JAR before any code existed. The later foundation prompt also said not to create a meaningless test merely to satisfy a checkbox. The resulting foundation stayed small and did not invent movie features or placeholder layers.

The environment complicated the apparently simple Java-version check. Java 25 was installed and available on `PATH`, while an inherited `JAVA_HOME` still pointed at JDK 17. Gradle toolchains could compile with Java 25 even when wrapper output showed a JDK 17 launcher. This distinction required manual interpretation: changing project code would not correct a parent process environment, and seeing “17” in one line did not by itself prove that Java 25 compilation had failed. Windows command invocation also produced non-project failures, including an unqualified wrapper command not being found and a quoted Java path being interpreted literally. Retrying with an explicit wrapper path or the Java executable on `PATH` was more useful than changing source files.

The AI was effective at inventorying the repository and turning constraints into an exact file plan. Human judgement was still needed to approve programmatic JavaFX, understand the environment mismatch, and decide that omitting a foundation-only unit test was better than testing a constant. Next time, I would capture `java --version`, `javac --version`, `JAVA_HOME`, and Gradle toolchain output together at the beginning so the distinction is documented once rather than rediscovered.

## Prompt example 2 — Persist before publish

The service-layer prompt did not merely ask Codex to “add persistence.” It specified a failure scenario: if saving failed after a mutation, in-memory state must not claim a change that was absent from disk. It suggested constructing proposed state, persisting it, and replacing current state only after success.

That detail shaped `MovieTrackerService.persistAndReplace`. Add, transition, remove, and rating operations build an immutable proposed list, call `Storage.save`, and assign it to `trackedMovies` only after the call succeeds. No-op operations such as adding an already tracked movie or marking an already watched movie return without saving. This prevents a subtle inconsistency: after a disk failure, the UI cannot show a movie as Watched while the persisted file still says Watchlist. A later restart therefore cannot unexpectedly reverse a change the UI had presented as successful.

The model could have implemented the simpler but unsafe order—mutate first, then save—if the prompt had only mentioned persistence generally. It also might have added rollback code, which would be harder to reason about if mutable objects or observers had already seen the new state. Persist-before-publish avoided that complexity.

Fake-storage tests made this decision verifiable without manipulating real files. Tests force `Storage.save` to throw and assert that Watchlist membership, Watched membership, and previous ratings remain unchanged. Other tests check that successful mutations save once and rejected/no-op operations do not save. Codex was particularly useful for generating this repetitive failure matrix. Human judgement remained necessary to choose the consistency policy and confirm that whole-state JSON writes were small enough for this ordering. Next time, I would state the desired transaction boundary even earlier, in the storage design, so service and UI prompts can refer to a named invariant from the start.

## Prompt example 3 — Auditing Phase 4 instead of rewriting it

The Phase 4 prompt began with an audit instruction: compare the existing TMDB client and tests against every checkbox, do not duplicate completed behavior, and add production code only for a real gap. This was important because the preceding TMDB-client increment had already implemented much of error handling while completing the happy path.

Without that constraint, an LLM may respond to a new “error handling” phase by introducing another wrapper exception, duplicating switches, or refactoring working code simply to demonstrate activity. The audit established that the existing `TmdbException` boundary already covered missing tokens, non-2xx responses, network failures, malformed payloads, and cause preservation. Explicit timeout classification and its user-facing mapping received focused verification. `TmdbErrorCategory.TIMEOUT` is tested with `HttpTimeoutException`, and the UI mapping distinguishes a slow response from a general connection failure.

There is an evidence limitation: storage, Phase 3, and Phase 4 appear together in commit `1d6ea67`, so Git cannot reconstruct exactly which lines existed immediately before the audit. It would be inaccurate to claim a separate timeout commit. The current implementation and tests demonstrate the final behavior, while the prompt record demonstrates the “audit first” intent.

This example shows where prompting can be more valuable as a restraint than as a code-generation request. Codex helped enumerate categories and tests, but a human still had to decide which distinctions were useful to users and whether the exception structure was already sufficient. In a future project, I would commit the happy path before starting the error audit; that would make the audit's actual delta independently reviewable.

## Prompt example 4 — Keeping network and persistence work off JavaFX

The Search and Details prompts repeatedly stated that TMDB work must not run on the JavaFX Application Thread. Later tracking prompts extended the same policy to persistence-backed mutations. They also required disabling duplicate actions, restoring controls after failure, ignoring stale task results, and cancelling work during navigation or shutdown.

Codex applied one injected application executor and JavaFX `Task` objects rather than moving asynchronous behavior into `TmdbClient` or `MovieTrackerApplicationService`. This kept the application-facing API synchronous and testable while making threading an integration responsibility of the UI. It also preserved a clear rule: domain, API, service, and storage code do not depend on JavaFX.

Automated tests could verify the synchronous services and error mappings but not convincingly prove GUI responsiveness, focus order, or that stale tasks never painted the wrong screen. The project intentionally avoided brittle JavaFX automation. Manual observation was therefore more valuable than prompting for another mock-heavy test. The same limitation applied to real TMDB calls: sandboxed/offline tests could validate requests and fixtures, but only a human-run token and network could verify the live service.

Next time, I would add a small documented manual-test checklist earlier and retain screenshots or notes for each GUI increment. AI can identify concurrency risks and generate handlers, but a person must still operate the actual desktop application under slow network, cancellation, and resize conditions.

## Prompt example 5 — Release portability and compliance

The release prompt explicitly warned against assuming that a Windows-built JavaFX fat JAR was cross-platform. This forced investigation of platform-specific JavaFX native libraries before making a submission claim. The result was a fat JAR containing application resources, Jackson, and Windows x86-64 JavaFX dependencies, plus a configurable Gradle property for other targets. The guides correctly state that Linux and macOS artifacts still require builds and smoke tests on matching systems.

This increment also exposed two limits of automated assistance. First, the initial release commit accidentally included `release/data/movies.json`, a runtime file that the prompt had prohibited. It was removed in commit `8b60a25`, and packaging/ignore rules were tightened. Second, documentation attribution did not initially put TMDB credit inside the application. A later compliance audit checked the current TMDB guidance, bundled an official logo, and added the exact non-endorsement notice to a token-independent About view.

The AI was strong at inspecting JAR contents, comparing packaging strategies, updating documentation, and checking the official wording. It could launch the Windows JAR, but it could not honestly verify Linux/macOS behavior or perform a reliable visual click-through of every JavaFX state. Human judgement was needed to reject a universal-JAR claim, notice the runtime-data mistake, assess the About layout, and decide what compliance claims were supportable.

Next time, I would make an explicit release allowlist—application classes/resources and named runtime dependencies—and test it automatically before the first release commit. I would also include third-party attribution as a release-definition item rather than discovering it during a final documentation audit.

## Overall assessment

Codex was most effective when tasks were bounded and acceptance criteria were concrete. It accelerated repetitive validation tests, DTO and mapping code, service edge-case coverage, documentation synchronization, and audits across many files. It also helped maintain layer boundaries because each prompt restated what the increment must not implement.

Its limitations were clearest around state outside source code: inherited environment variables, network access, visual JavaFX behavior, operating-system-specific native libraries, and the difference between a successful command and a genuinely verified user flow. It could also make ordinary command mistakes and could include an unintended runtime file despite an explicit prohibition. Those failures were recoverable because increments were small, Git changes were inspected, and follow-up prompts named the observed problem precisely.

Human responsibility remained central. The student selected scope, approved architecture, interpreted MP1 requirements, reviewed UX, supplied and protected the real TMDB credential, decided which cross-platform claims were honest, and accepted or rejected generated changes. A useful lesson is that detailed prompting does not replace review; it makes review narrower and gives failures clearer boundaries.

## AI-use declaration

OpenAI Codex was used during this project for repository inspection, planning, code generation, test generation, debugging, documentation drafting, release-packaging analysis, and review/refinement. Work was directed through incremental prompts tied to the project specifications. The student remains responsible for understanding, verifying, correcting, and submitting the final application and documentation. No TMDB credential was supplied to or stored in the repository documentation.
