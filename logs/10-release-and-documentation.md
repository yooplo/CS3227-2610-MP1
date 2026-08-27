# Development Log 10 — Release, Guides, and Attribution

## Objective

Build the MP1 submission JAR, document the implemented application accurately, and complete in-application TMDB attribution.

## Prompt constraints and approach

The packaging prompt required `java -jar release/MovieTracker.jar`, the existing plain `Launcher`, bundled Jackson/JavaFX runtime libraries, no credentials/tests/local data, and honest analysis of JavaFX native portability. The documentation prompt required implementation-based User and Developer Guides, acknowledgements, and an AI-use statement. A later compliance prompt required TMDB's exact current notice and an official approved logo inside the application.

The AI added a reproducible fat-JAR task with platform selection, documented the checked-in artifact as Windows x86-64, and avoided claiming unverified Linux/macOS compatibility. The guides describe user flows, architecture, JSON format, errors, tests, and packaging. The About view bundles TMDB's official primary-long blue SVG and shows: “This product uses the TMDB API but is not endorsed or certified by TMDB.” It has no API, storage, state, or token dependency.

## Assumptions, issues, and corrections

- Investigation showed that JavaFX native libraries are OS/architecture-specific, so a single Windows-built universal JAR claim would have been inaccurate.
- Commit `3657216` accidentally included `release/data/movies.json`. Commit `8b60a25` removed it and adjusted build/ignore rules so runtime user data is not part of the release.
- The sandbox could launch the JavaFX JAR and verify that the eagerly constructed About view found and parsed its logo, but it could not perform a reliable visual click-through or Linux/macOS smoke test.
- Current Git history preserves documentation and attribution commits, but not full raw AI transcripts; these files therefore summarize rather than quote unrecoverable interactions.

## Verification and human judgement

The release was tested, built, inspected for classes/resources and accidental files, copied/launched from a clean directory, and checked for token-free startup. The attribution increment added tests for exact wording and resource presence and verified both inside the rebuilt JAR. Human judgement remained essential for interpreting the submission artifact requirement, limiting platform claims, reviewing guides, and approving TMDB branding presentation.

## Resulting commits

- `3657216` — `Add Windows release packaging`
- `12435d8` — `Update user and developer documentation`
- `8b60a25` — `Fix-UI-text-encoding`
- `d3aff1c` — `Add TMDB attribution`

## Later cross-platform verification

After the release and documentation work described above, runtime smoke testing was completed successfully on Windows, macOS, and Linux. JavaFX packaging remains platform-specific: this verification does not make the checked-in Windows x86-64 JAR universal, and each target still requires matching JavaFX native libraries.
