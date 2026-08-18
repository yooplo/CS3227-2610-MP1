# Movie Tracker

Movie Tracker is a Java desktop application that helps users discover movies and keep track of movies they are interested in watching.

This project is developed for **CS3227 Mini Project 1 (MP1): AI-assisted Software Engineering**.

## Features

Movie Tracker aims to provide the following features:

- **Search for movies** — Search for movies by title.
- **View movie details** — View information such as the movie title, release date, overview, genres, and rating.
- **Watchlist** — Add movies that you are interested in watching to a personal watchlist.
- **Watched movies** — Mark movies as watched to keep track of your viewing history.
- **Personal ratings** — Give watched movies your own rating.
- **Notes** — Add personal notes about movies.
- **Movie discovery** — Browse popular or trending movies.
- **Search and filtering** — Find movies in your collection based on their title, status, or other properties.
- **Persistent storage** — Save your movie collection locally so that it is available the next time the application is opened.

> [!NOTE]
> Movie Tracker is currently under development. Features listed above may change as the project progresses.

## User Interface

Movie Tracker is implemented as a **Java desktop application**.

The application will provide a graphical interface for browsing movies and managing the user's movie collection.

## Movie Information

Movie information is retrieved from an external movie API.

Information retrieved may include:

- Movie title
- Poster
- Release date
- Overview
- Genres
- Rating
- Popularity

API details and setup instructions will be documented once the API integration has been finalised.

## Getting Started

### Prerequisites

To build and run Movie Tracker, you will need:

- Java
- Gradle

The exact Java version and additional setup requirements will be updated as development progresses.

### Running the Application

Clone the repository:

```bash
git clone <repository-url>
cd CS3227-2610-MP1
```

Run the application using Gradle:

```bash
./gradlew run
```

On Windows:

```powershell
.\gradlew run
```

## Project Structure

```text
CS3227-2610-MP1/
├── src/
│   ├── main/
│   │   └── java/
│   └── test/
│       └── java/
├── docs/
│   ├── UserGuide.md
│   ├── DeveloperGuide.md
│   └── Reflections.md
├── logs/
├── build.gradle
└── README.md
```

The project structure may evolve as development progresses.

## Documentation

Additional project documentation can be found in the `docs` directory:

- [`docs/UserGuide.md`](docs/UserGuide.md) — Instructions for setting up and using Movie Tracker.
- [`docs/DeveloperGuide.md`](docs/DeveloperGuide.md) — Design, architecture, development process, and acknowledgements.
- [`docs/Reflections.md`](docs/Reflections.md) — Reflections on the use of LLMs and prompting during development.

AI interaction summaries are stored in:

```text
logs/
```

## Testing

Automated tests will be placed under:

```text
src/test/java/
```

Tests can be run using:

```bash
./gradlew test
```

On Windows:

```powershell
.\gradlew test
```

## AI-Assisted Development

This project is developed as part of an AI-assisted software engineering exercise.

AI tools may be used during development for activities such as:

- Brainstorming and refining features
- Exploring alternative software designs
- Generating and reviewing code
- Debugging
- Creating and reviewing tests
- Improving documentation
- Summarising development interactions

AI-generated suggestions and code are reviewed and verified before being incorporated into the project.

Detailed reflections on the use of AI can be found in [`docs/Reflections.md`](docs/Reflections.md).

## Acknowledgements

This project is developed for **CS3227 Software Engineering on Modern Application Platforms** at the National University of Singapore.

External libraries, APIs, code, documentation, and other resources used by the project will be acknowledged in the Developer Guide.