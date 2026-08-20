# Movie Tracker — Project Specification

**Module:** CS3227  
**Project:** Mini Project 1 (MP1): AI-assisted Software Engineering  
**Application:** Movie Tracker  
**Document:** `SPEC.md`

---

## 1. Purpose of This Document

This document is the **source of truth** for the Movie Tracker project.

All implementation decisions, AI-generated code, documentation, tests, and future changes should comply with this specification.

If another document, AI suggestion, implementation, or assumption conflicts with this specification, **this specification takes precedence**, except where it conflicts with an official CS3227 MP1 requirement.

The specification should be updated deliberately when requirements change.

---

# 2. Project Overview

Movie Tracker is a **Java desktop application** that allows a user to discover movies and maintain a personal movie collection.

The application should allow users to:

1. Search for movies.
2. View information about a movie.
3. Add movies to a personal watchlist.
4. Mark movies as watched.
5. Rate watched movies.
6. Add personal notes.
7. Browse and manage their movie collection.
8. Retain their collection between application sessions.

Movie metadata may be retrieved from an external movie API.

The application is intended primarily as a **personal utility application for a single user**.

---

# 3. MP1 Course Requirements

The following requirements originate from the CS3227 MP1 specification and are **mandatory**.

## 3.1 Individual Project

The project must be completed individually.

It must not be submitted as a team project.

---

## 3.2 Application Type

The application must be a:

> **Java desktop application**

A web application alone does not satisfy this requirement.

The primary application should therefore run locally on the user's computer.

---

## 3.3 Original Functionality

The project must not replicate the functionality of the CS2103/T individual project or team project.

In particular, the application must **not become a generic to-do/task manager with a chat interface**.

Movie Tracker must remain focused on movie discovery and movie collection management.

---

## 3.4 AI-Assisted Development

The development process should make meaningful use of:

- Large Language Models (LLMs)
- Prompting
- AI-assisted software engineering

Agentic development features may also be used.

AI should assist the development process rather than replace engineering judgement.

AI-generated output must be reviewed and verified before being accepted.

---

# 4. Repository Requirements

## 4.1 Repository Name

The GitHub repository must be named:

```text
CS3227-2610-MP1
```

---

## 4.2 Repository Visibility

The repository must be:

```text
Public
```

at the time required for grading.

---

## 4.3 Grading Branch

The grading branch is:

```text
master
```

The latest submission-ready version of the application must be available on `master`.

Do not rely on `main` being graded.

Feature branches may be used during development, but completed features intended for submission must eventually be merged into `master`.

---

# 5. Required Repository Structure

At minimum, the repository should contain:

```text
CS3227-2610-MP1/
├── src/
│   ├── main/
│   │   └── java/
│   └── test/
│       └── java/
│
├── docs/
│   ├── UserGuide.md
│   ├── DeveloperGuide.md
│   └── Reflections.md
│
├── logs/
│
├── README.md
├── SPEC.md
├── build.gradle
└── ...
```

`src/main/java` should remain the Java source root.

Additional files and directories may be added where appropriate.

---

# 6. Required Documentation

## 6.1 User Guide

Location:

```text
docs/UserGuide.md
```

The User Guide must describe:

- All current user-facing features.
- How to set up the application.
- How to run the application.
- How to use the application.
- How the application can be tested by a peer tester.

Descriptions must accurately match the released application.

A documented feature that does not behave as documented may be considered a bug.

---

## 6.2 Developer Guide

Location:

```text
docs/DeveloperGuide.md
```

The Developer Guide should describe:

- Overall architecture.
- Important design decisions.
- Relevant classes/components.
- Storage design.
- API integration where applicable.
- Testing approach.
- Software engineering process.

It must also contain an acknowledgement section for reused:

- Ideas
- Code
- Documentation
- Libraries
- External resources

where acknowledgement is appropriate.

---

## 6.3 AI Reflections

Location:

```text
docs/Reflections.md
```

The document must reflect on the use of AI-assisted software engineering.

It must contain at least **three interesting prompt examples** and explain them in detail.

Possible reflection topics include:

- Why was the prompt formulated that way?
- What assumptions did the AI make?
- What did the AI get wrong?
- How was the result verified?
- How did the prompt evolve?
- When was prompting less effective than manual work?
- What engineering judgement was still required?
- What would be done differently next time?

The reflection should discuss actual development experiences rather than generic observations about AI.

---

## 6.4 AI Interaction Logs

Location:

```text
logs/
```

The directory should contain summaries of prompts and AI interactions that occurred during development.

These summaries are intended to help reconstruct the development process when writing the reflection.

AI may generate the summaries, but their correctness must be verified.

A possible organization is:

```text
logs/
├── 001-project-planning.md
├── 002-project-setup.md
├── 003-movie-search.md
├── 004-watchlist.md
├── 005-storage.md
└── ...
```

The exact naming convention is not mandatory unless later specified.

---

# 7. Product Requirements

## 7.1 Core Domain

The central domain object is a **Movie**.

A movie may contain information such as:

```text
Movie
├── external ID
├── title
├── release date
├── overview
├── genres
├── poster
├── external rating
├── watch status
├── personal rating
└── personal notes
```

Not every field is required to be available from the first implementation.

---

# 8. Movie Search

## FR-1: Search for Movies

The user must be able to search for movies using a movie title or title keyword.

Example:

```text
Search: Interstellar
```

The application should display matching movies returned by the configured movie information source.

---

## FR-2: Search Results

Each search result should provide enough information for the user to distinguish between movies.

At minimum, results should display:

- Movie title

Where available, the application should additionally display:

- Poster
- Release year/date
- Rating

---

## FR-3: Empty Search

The application must handle an empty search gracefully.

It must not crash.

The user should receive appropriate feedback.

---

## FR-4: No Search Results

If no movies match a search query, the application should display an appropriate message rather than an empty or broken interface.

---

# 9. Movie Details

## FR-5: View Movie Details

The user must be able to select a movie and view its details.

Movie details may include:

- Title
- Poster
- Release date
- Overview
- Genres
- External rating
- Other relevant movie metadata

The exact information available depends on the external API.

---

# 10. Watchlist

## FR-6: Add Movie to Watchlist

The user must be able to add a movie to their watchlist.

A movie already present in the user's collection should not accidentally produce duplicate entries.

---

## FR-7: View Watchlist

The user must be able to view movies currently on their watchlist.

---

## FR-8: Remove from Watchlist

The user must be able to remove a movie from their watchlist.

Removing a movie must affect the intended movie only.

---

# 11. Watched Movies

## FR-9: Mark Movie as Watched

The user must be able to mark a movie as watched.

A watched movie should be distinguishable from a movie that is still on the watchlist.

---

## FR-10: View Watched Movies

The user must be able to view movies they have marked as watched.

---

## FR-11: Change Watch Status

Where supported by the final UI, the user may change a movie from watched back to unwatched/watchlist status.

This is desirable but not mandatory for the first functional version.

---

# 12. Personal Ratings

## FR-12: Rate Movie

The user should be able to assign a personal rating to a movie.

The rating scale must be consistent throughout the application.

The chosen scale must be documented in the User Guide.

Example possibilities include:

```text
1–5
```

or:

```text
1–10
```

The final scale must be chosen before this feature is considered complete.

---

## FR-13: Update Rating

The user should be able to change an existing personal rating.

---

## FR-14: Rating Validation

The application must reject ratings outside the supported range.

Invalid input must not corrupt saved movie data.

---

# 13. Personal Notes

## FR-15: Add Notes

The user should be able to attach personal notes to a movie.

Example:

```text
Great soundtrack and cinematography.
Rewatch the docking scene.
```

---

## FR-16: Edit Notes

The user should be able to update their notes.

---

## FR-17: Persist Notes

Personal notes should remain available after restarting the application.

---

# 14. Movie Collection

## FR-18: View Collection

The user should be able to browse movies they have saved.

The collection should clearly distinguish relevant states such as:

- Watchlist
- Watched

---

## FR-19: Find Movies in Collection

The user should be able to find movies in their local collection.

At minimum, title-based searching should be supported.

---

## FR-20: Filtering

Filtering by watch status is desirable.

Example filters:

```text
All
Watchlist
Watched
```

Additional filters are optional.

---

# 15. Movie Discovery

Movie discovery is a secondary feature.

## FR-21: Browse Movies

Where supported by the selected API, the application may allow the user to browse categories such as:

- Popular movies
- Trending movies
- Now playing
- Top-rated movies

At least one discovery view is desirable but is not part of the minimum viable product unless later promoted to a mandatory requirement.

---

# 16. Persistence

## FR-22: Save User Data

The application must persist user-owned movie data locally.

This includes, where implemented:

- Saved movies
- Watch status
- Personal ratings
- Personal notes

---

## FR-23: Load User Data

Saved data must automatically be loaded when the application starts.

---

## FR-24: Missing Storage

The application must handle the case where the storage file or storage directory does not yet exist.

The first launch of the application must not fail simply because no previous data exists.

---

## FR-25: Storage Paths

Local file paths must be:

- Relative rather than machine-specific absolute paths.
- OS-independent where possible.

Code must not assume paths such as:

```text
C:\Users\SomeUser\...
```

---

## FR-26: Corrupted Data

Where reasonably possible, corrupted or malformed stored data should be handled gracefully.

The application should avoid crashing without explanation.

---

# 17. External API

## 17.1 API Choice

The project may use an external movie API.

There is no MP1 restriction on which API is used.

The exact API should be recorded in the Developer Guide once selected.

---

## FR-27: API Integration

Movie metadata retrieved from the external API must be converted into internal application objects rather than allowing API-specific representations to unnecessarily leak throughout the application.

---

## FR-28: API Failure

The application must handle common API failures gracefully.

Examples include:

- No internet connection.
- Request timeout.
- Invalid response.
- HTTP error.
- API rate limit.
- Invalid or missing API credentials.

The application must not terminate unexpectedly because a movie request fails.

---

## FR-29: API Key Security

If the selected API requires a secret API key, the key must not be committed directly into source code or publicly exposed in the repository.

The application should obtain sensitive configuration from an appropriate external configuration mechanism.

Setup instructions must be documented.

---

# 18. GUI Requirements

## 18.1 Java Desktop GUI

Movie Tracker must provide a graphical desktop interface.

JavaFX is the preferred GUI framework for this project.

---

## FR-30: Core Features Accessible Through GUI

Core user functionality must be accessible through the graphical interface.

The user should not need to modify files manually to perform normal application operations.

---

## FR-31: Error Feedback

Errors should be displayed clearly to the user.

Examples:

```text
Unable to connect to movie service.
```

```text
No movies found for "abcxyz".
```

```text
Rating must be between 1 and 5.
```

The GUI must not silently ignore important errors.

---

## FR-32: Responsive Layout

The interface should remain usable when the window is resized within reasonable desktop dimensions.

Controls should not unnecessarily overlap or disappear.

---

# 19. Suggested GUI Structure

The exact design may evolve.

A possible layout is:

```text
┌───────────────────────────────────────────────────────┐
│ Movie Tracker                                  Search │
├───────────────┬───────────────────────────────────────┤
│               │                                       │
│ Discover      │            Main Content               │
│ Search        │                                       │
│ Watchlist     │     Movie cards / movie details       │
│ Watched       │                                       │
│               │                                       │
└───────────────┴───────────────────────────────────────┘
```

This diagram is illustrative rather than mandatory.

GUI changes are permitted provided all required functionality remains available.

---

# 20. Architecture Requirements

The code should follow object-oriented design principles.

Responsibilities should be separated rather than placing the entire application in one class.

Possible components include:

```text
MovieTracker
│
├── UI
│
├── Model
│   ├── Movie
│   └── MovieCollection
│
├── Service
│   └── MovieApiService
│
├── Storage
│   └── MovieStorage
│
└── Utility / Configuration
```

Exact class and package names are not mandated by this specification.

The architecture may evolve as implementation requirements become clearer.

---

# 21. Package Structure

Classes should be organized into suitable Java packages.

A possible structure is:

```text
src/main/java/
└── movietracker/
    ├── MovieTracker.java
    ├── ui/
    ├── model/
    ├── service/
    ├── storage/
    └── exception/
```

This is a recommendation rather than a fixed requirement.

`src/main/java` must remain the source root.

---

# 22. Build System

The project should use **Gradle** for build automation.

Gradle should support at least:

- Building the application.
- Running the application.
- Running automated tests.

Example commands:

```bash
./gradlew build
./gradlew run
./gradlew test
```

Windows equivalents may use:

```powershell
.\gradlew build
.\gradlew run
.\gradlew test
```

---

# 23. Testing Requirements

Testing is an important part of the project.

## 23.1 JUnit

JUnit should be used for automated testing of non-trivial application logic.

Tests should focus particularly on:

- Movie collection operations.
- Duplicate handling.
- Rating validation.
- Search/filter logic.
- Storage serialization/deserialization.
- API response conversion.
- Error handling where practical.

---

## 23.2 Test Location

Tests should follow Gradle conventions:

```text
src/test/java/
```

---

## 23.3 GUI Testing

GUI behavior that is difficult to test automatically may be tested manually.

Important GUI workflows should still be verified before release.

---

# 24. Error Handling

The application should anticipate reasonable errors rather than assuming all input and external services are valid.

Examples include:

### User Input

- Empty searches
- Invalid ratings
- Invalid operations
- Duplicate movies

### Storage

- Missing files
- Missing directories
- Malformed data
- Permission problems

### API

- Network unavailable
- API unavailable
- Invalid credentials
- Rate limiting
- Unexpected response data

Errors should be handled at an appropriate abstraction level and presented to the user where relevant.

---

# 25. Code Quality

Code should:

- Use meaningful names.
- Avoid excessively long methods.
- Avoid unnecessary duplication.
- Keep classes focused on clear responsibilities.
- Use appropriate abstractions.
- Follow consistent formatting.
- Avoid magic numbers and unexplained constants.
- Handle errors intentionally.
- Be reasonably easy for another developer to understand.

Refactoring should be performed when implementation growth causes responsibilities to become unclear.

---

# 26. Version Control Practices

Development must use Git.

Commits should represent meaningful development increments where practical.

Example:

```text
Add basic movie model
Add movie search API integration
Add watchlist storage
Add watched status
Add movie rating validation
Add JavaFX movie search interface
```

Avoid meaningless commit messages such as:

```text
stuff
changes
update
final
final2
```

Feature branches are allowed.

The submission-ready application must ultimately be merged into:

```text
master
```

---

# 27. AI Development Rules

AI may be used for:

- Requirement analysis
- Brainstorming
- Architecture exploration
- Implementation
- Refactoring
- Debugging
- Test generation
- Test review
- Documentation
- Code review
- Prompt summarization

However, AI-generated work must not automatically be assumed correct.

For significant AI-generated changes:

1. Understand the proposed change.
2. Inspect the generated code.
3. Verify it against this specification.
4. Run relevant tests.
5. Manually test where appropriate.
6. Correct problems before committing.
7. Record meaningful AI interactions for later reflection.

---

# 28. AI Source-of-Truth Rule

When asking an AI coding assistant to modify this repository, it should be instructed to read this specification first.

Recommended instruction:

```text
Before making changes, read SPEC.md and treat it as the project's source of truth.

Do not introduce requirements, dependencies, architectural changes, or user-facing features that conflict with SPEC.md.

If my request conflicts with SPEC.md, point out the conflict before implementing it.

Do not silently modify SPEC.md to make an implementation fit the specification.
```

AI should not invent new requirements simply because they appear useful.

---

# 29. Requirement Change Process

This specification is expected to evolve.

When changing project requirements:

1. Decide on the new requirement.
2. Update `SPEC.md`.
3. Update the implementation.
4. Update relevant tests.
5. Update the User Guide if user-facing behavior changed.
6. Update the Developer Guide if design or architecture changed.
7. Record significant AI-assisted decisions for reflection where relevant.

Implementation and documentation should eventually agree with this specification.

---

# 30. Minimum Viable Product

The **minimum viable Movie Tracker** should support the following end-to-end workflow:

```text
Launch application
        ↓
Search for movie
        ↓
View search results
        ↓
View movie details
        ↓
Add movie to watchlist
        ↓
View watchlist
        ↓
Mark movie as watched
        ↓
Restart application
        ↓
Saved movie and status remain available
```

The MVP therefore requires:

1. Java desktop GUI.
2. Movie search.
3. Movie details.
4. Watchlist.
5. Watched status.
6. Local persistence.
7. Basic error handling.
8. External movie information integration.

The following features may be implemented after the MVP:

- Personal ratings.
- Personal notes.
- Trending/popular movies.
- Advanced filtering.
- Sorting.
- Additional movie discovery features.
- Additional GUI polish.

---

# 31. Non-Goals

Unless this specification is explicitly changed, Movie Tracker is **not intended to be**:

- A generic task manager.
- A calendar application.
- A social network.
- A movie streaming application.
- A video player.
- A movie piracy/download application.
- A multi-user account system.
- A web application replacing the required Java desktop application.
- A full clone of an existing movie platform.

The application tracks and discovers movies; it does not provide the movies themselves.

---

# 32. Release Requirements

Before the final MP1 submission, verify:

- [ ] Repository is named `CS3227-2610-MP1`.
- [ ] Repository is public.
- [ ] Submission-ready code is on `master`.
- [ ] Application is a Java desktop application.
- [ ] Application builds successfully.
- [ ] Application launches successfully.
- [ ] Core movie search works.
- [ ] Movie details work.
- [ ] Watchlist works.
- [ ] Watched status works.
- [ ] Persistence works after restarting.
- [ ] API/network errors are handled.
- [ ] Important functionality has been tested.
- [ ] `docs/UserGuide.md` is complete and accurate.
- [ ] `docs/DeveloperGuide.md` is complete and accurate.
- [ ] `docs/Reflections.md` contains at least three detailed prompt examples.
- [ ] `logs/` contains verified summaries of AI interactions.
- [ ] Reused ideas/code/documentation are acknowledged.
- [ ] No secret API keys are committed.
- [ ] README matches the current product.
- [ ] Documentation matches the latest release.
- [ ] Final `master` branch is pushed to GitHub.

---

# 33. Priority Order

When development time is limited, prioritize work in the following order:

```text
1. Correctness
2. MP1 mandatory requirements
3. Core movie-tracking functionality
4. Persistence and reliability
5. Code quality
6. Testing
7. Documentation
8. GUI usability
9. Additional features
10. Cosmetic polish
```

A smaller application whose features work correctly is preferable to a large application containing many incomplete features.

---

# 34. Current Scope Summary

The intended Movie Tracker application is:

> A Java desktop application that retrieves movie information from an external movie service and allows a single user to maintain a persistent personal watchlist and viewing history.

The core scope is:

```text
Search
   ↓
Movie Details
   ↓
Watchlist
   ↓
Watched
   ↓
Persistent Local Collection
```

Personal ratings, notes, discovery features, filtering, and additional polish extend this core functionality but should not compromise completion of the core workflow.

---

# 35. Specification Authority

The following precedence should be used when requirements conflict:

```text
Official CS3227 MP1 requirements
            ↓
        SPEC.md
            ↓
   DeveloperGuide.md
            ↓
      UserGuide.md
            ↓
        README.md
            ↓
Implementation assumptions / AI suggestions
```

Official course requirements always take precedence over this document.

`SPEC.md` takes precedence over project-level assumptions and AI-generated suggestions.

When a conflict is discovered, resolve it explicitly rather than silently choosing one interpretation.