# Movie Tracker — Repository Setup

## Target repository structure

```text
movie-tracker/
├── AGENTS.md
├── README.md
├── REPO_SETUP.md
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
├── .gitignore
├── specs/
│   ├── README.md
│   ├── requirements.md
│   ├── design.md
│   ├── api.md
│   └── tasks.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── movietracker/
│   │   └── resources/
│   │       └── movietracker/
│   └── test/
│       ├── java/
│       │   └── movietracker/
│       └── resources/
│           └── fixtures/
├── data/
│   └── .gitkeep
├── docs/
│   └── README.md
└── release/
    └── .gitkeep
```

## Git setup

If creating a new repository locally:

```bash
git init
git branch -M master
git add .
git commit -m "Set up Movie Tracker project"
git remote add origin <YOUR_GITHUB_REPO_URL>
git push -u origin master
```

If GitHub created a repository with `main`, rename the local branch before pushing:

```bash
git branch -M master
git push -u origin master
```

Then set `master` as the default branch on GitHub and remove `main` only after verifying `master` contains the correct history.

## Java

Install JDK 25 and verify:

```bash
java --version
javac --version
```

Both should report Java 25.

## Gradle

Prefer the Gradle Wrapper so graders/developers do not need a matching system Gradle installation.

Typical commands:

```bash
./gradlew run
./gradlew test
./gradlew clean build
```

Windows PowerShell/CMD:

```powershell
.\gradlew.bat run
.\gradlew.bat test
.\gradlew.bat clean build
```

## TMDB token

Create a TMDB API Read Access Token from your TMDB account.

Set it as an environment variable named `TMDB_API_TOKEN`.

PowerShell for current terminal session:

```powershell
$env:TMDB_API_TOKEN="your_token_here"
```

macOS/Linux shell for current terminal session:

```bash
export TMDB_API_TOKEN="your_token_here"
```

Do not commit the token.

## Recommended `.gitignore`

At minimum ignore:

```text
.gradle/
build/
out/
.idea/
.vscode/
*.iml
.env
*.local.properties
_temp/
data/*.json
!data/.gitkeep
```

Whether `release/*.jar` is ignored depends on the exact MP1 submission requirement. For this MP1, the supplied instructions say the submission repository should contain the latest JAR under `release/`, so keep the required submission JAR tracked for the final submission if that is how grading obtains it.

## First implementation checkpoint

Do not begin by building every screen.

First get this minimal slice working:

1. Java 25 + Gradle builds.
2. JavaFX window launches.
3. Separate `Launcher` is present.
4. `TMDB_API_TOKEN` can be read without printing it.
5. JUnit can run one trivial test.
6. Commit this checkpoint before implementing movie features.
