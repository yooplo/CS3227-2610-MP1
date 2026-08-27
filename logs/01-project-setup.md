# Development Log 01 — Project Setup and Foundation

## Objective

Establish the repository and then create the smallest Java 25, Gradle, and JavaFX application foundation without implementing Movie Tracker features.

## Prompt constraints and approach

The first prompt deliberately separated inspection from implementation. It required reading `AGENTS.md`, every specification, repository setup notes, build files, and the existing tree, and explicitly prohibited edits. A follow-up authorized only the foundation: Gradle Wrapper, Java 25 toolchain, JavaFX, JUnit 5, the `movietracker` package, `Launcher`, and a placeholder `MovieTrackerApp`. It also prohibited a meaningless test merely to fill a checklist.

The AI proposed a plain Java launcher in front of the JavaFX `Application`, programmatic JavaFX for the minimal window, and Gradle toolchains for Java 25. No movie, API, storage, or controller code was introduced at this stage.

## Assumptions, issues, and corrections

- The working environment exposed a stale `JAVA_HOME` pointing to JDK 17 even though Java 25 was installed and available. Gradle's Java 25 toolchain configuration became the authoritative compilation target, but wrapper-launch output continued to make the inherited JDK 17 setting visible.
- The initial foundation did not manufacture a trivial unit test because there was no meaningful non-GUI foundation method to test.
- Command invocation on Windows required care around `gradlew.bat` paths and inherited environment variables. Later verification repeated this lesson when an unqualified wrapper command and a quoted Java path failed before being corrected.

## Verification and human judgement

The requested checks were Gradle tests, a full build, and a JavaFX launch. Git records the completed build skeleton and task checkboxes, but it does not preserve the complete terminal transcript. Human judgement was required to accept the separate launcher pattern, keep the initial UI intentionally minimal, and distinguish the Gradle daemon JVM from the Java 25 compilation toolchain.

## Resulting commits

- `22788d8` — `Set up Movie Tracker project`
- `96c969b` — `Set up JavaFX project foundation`
