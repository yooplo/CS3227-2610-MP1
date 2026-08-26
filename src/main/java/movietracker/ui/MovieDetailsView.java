package movietracker.ui;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import movietracker.api.TmdbErrorCategory;
import movietracker.api.TmdbException;
import movietracker.model.Movie;
import movietracker.model.MovieDetails;
import movietracker.model.WatchStatus;
import movietracker.service.MovieTrackerApplicationService;
import movietracker.storage.StorageException;

/**
 * Loads and displays detailed TMDB information for one selected movie.
 */
final class MovieDetailsView extends BorderPane {

    private static final double POSTER_WIDTH = 210;
    private static final double POSTER_HEIGHT = 315;

    private final Movie selectedMovie;
    private final MovieTrackerApplicationService applicationService;
    private final Executor executor;
    private final Runnable backAction;
    private final StackPane contentArea = new StackPane();

    private Task<MovieDetails> activeLoad;
    private Task<Boolean> activeTrackingMutation;

    MovieDetailsView(Movie selectedMovie,
                     MovieTrackerApplicationService applicationService,
                     Executor executor,
                     String backDestination,
                     Runnable backAction) {
        this.selectedMovie = Objects.requireNonNull(selectedMovie, "selectedMovie");
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(backDestination, "backDestination");
        this.backAction = Objects.requireNonNull(backAction, "backAction");

        getStyleClass().add("details-view");
        setPadding(new Insets(20, 28, 28, 28));

        Button backButton = new Button("Back to " + backDestination);
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(event -> backAction.run());
        setTop(backButton);
        BorderPane.setMargin(backButton, new Insets(0, 0, 16, 0));

        setCenter(contentArea);
        loadDetails();
    }

    void cancelActiveLoad() {
        if (activeLoad != null) {
            activeLoad.cancel(true);
        }
        if (activeTrackingMutation != null) {
            activeTrackingMutation.cancel(true);
        }
    }

    private void loadDetails() {
        if (activeLoad != null && !activeLoad.isDone()) {
            return;
        }

        Task<MovieDetails> detailsTask = new Task<>() {
            @Override
            protected MovieDetails call() throws TmdbException {
                return applicationService.getMovieDetails(selectedMovie.getTmdbId());
            }
        };
        activeLoad = detailsTask;
        showLoading();

        detailsTask.setOnSucceeded(event -> {
            showDetails(detailsTask.getValue());
            finishLoad(detailsTask);
        });
        detailsTask.setOnFailed(event -> {
            Throwable failure = detailsTask.getException();
            String message = failure instanceof TmdbException tmdbException
                    ? TmdbErrorMessages.forCategory(tmdbException.getCategory())
                    : "Movie details could not be loaded. Try again.";
            showFailure(message);
            finishLoad(detailsTask);
        });
        detailsTask.setOnCancelled(event -> {
            showFailure(TmdbErrorMessages.forCategory(TmdbErrorCategory.INTERRUPTED));
            finishLoad(detailsTask);
        });

        executor.execute(detailsTask);
    }

    private void showLoading() {
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(42, 42);
        Label message = new Label("Loading details for " + selectedMovie.getTitle() + "…");
        VBox loading = new VBox(12, progress, message);
        loading.getStyleClass().add("details-state");
        loading.setAlignment(Pos.CENTER);
        contentArea.getChildren().setAll(loading);
    }

    private void showFailure(String message) {
        Label error = new Label(message);
        error.setWrapText(true);
        Button retry = new Button("Try again");
        retry.setOnAction(event -> loadDetails());
        VBox failure = new VBox(12, error, retry);
        failure.getStyleClass().add("details-state");
        failure.setAlignment(Pos.CENTER);
        contentArea.getChildren().setAll(failure);
    }

    private void showDetails(MovieDetails details) {
        VBox text = createDetailsText(details);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox summary = new HBox(24, new PosterView(
                details.getMovie().getPosterPath(), POSTER_WIDTH, POSTER_HEIGHT), text);
        summary.setAlignment(Pos.TOP_LEFT);

        VBox detailsContent = new VBox(20, summary);
        detailsContent.getStyleClass().add("details-content");

        ScrollPane scrollPane = new ScrollPane(detailsContent);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("details-scroll");
        contentArea.getChildren().setAll(scrollPane);
    }

    private VBox createDetailsText(MovieDetails details) {
        Movie movie = details.getMovie();
        Label title = new Label(movie.getTitle());
        title.getStyleClass().add("details-title");
        title.setWrapText(true);

        Label releaseDate = createMetadata(
                "Release", movie.getReleaseDate().map(LocalDate::toString).orElse("Unavailable"));
        Label runtime = createMetadata("Runtime", details.getRuntimeMinutes().isPresent()
                ? details.getRuntimeMinutes().getAsInt() + " minutes"
                : "Unavailable");
        Label genres = createMetadata("Genres", details.getGenres().isEmpty()
                ? "Unavailable"
                : String.join(", ", details.getGenres()));
        Label voteAverage = createMetadata("TMDB rating", details.getTmdbVoteAverage().isPresent()
                ? String.format(Locale.ROOT, "%.1f / 10", details.getTmdbVoteAverage().getAsDouble())
                : "Unavailable");

        Label overviewHeading = new Label("Overview");
        overviewHeading.getStyleClass().add("details-subheading");
        Label overview = new Label(details.getOverview().orElse("Overview unavailable."));
        overview.getStyleClass().add("details-overview");
        overview.setWrapText(true);

        VBox trackingActions = new VBox(8);
        trackingActions.getStyleClass().add("tracking-actions");
        refreshTrackingActions(trackingActions, movie, null, false);

        VBox text = new VBox(
                10, title, releaseDate, runtime, genres, voteAverage,
                overviewHeading, overview, trackingActions);
        text.setMinWidth(0);
        return text;
    }

    private void refreshTrackingActions(VBox actions,
                                        Movie movie,
                                        String feedback,
                                        boolean errorFeedback) {
        actions.getChildren().clear();

        Label heading = new Label("Tracking");
        heading.getStyleClass().add("details-subheading");
        actions.getChildren().add(heading);

        applicationService.getTrackingStatus(movie.getTmdbId()).ifPresentOrElse(status -> {
            Label state = new Label(status == WatchStatus.WATCHLIST
                    ? "In Watchlist"
                    : "Already Watched");
            state.getStyleClass().add("tracking-state");
            actions.getChildren().add(state);
        }, () -> {
            Button addButton = new Button("Add to Watchlist");
            addButton.getStyleClass().add("primary-action");
            addButton.setOnAction(event -> addToWatchlist(actions, movie));
            actions.getChildren().add(addButton);
        });

        if (feedback != null) {
            Label message = new Label(feedback);
            message.setWrapText(true);
            message.getStyleClass().add(errorFeedback
                    ? "tracking-error"
                    : "tracking-feedback");
            actions.getChildren().add(message);
        }
    }

    private void addToWatchlist(VBox actions, Movie movie) {
        if (activeTrackingMutation != null && !activeTrackingMutation.isDone()) {
            return;
        }

        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(24, 24);
        Label message = new Label("Adding to Watchlist…");
        HBox addingState = new HBox(10, progress, message);
        addingState.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("Tracking");
        heading.getStyleClass().add("details-subheading");
        actions.getChildren().setAll(heading, addingState);

        Task<Boolean> addTask = new Task<>() {
            @Override
            protected Boolean call() throws StorageException {
                return applicationService.addToWatchlist(movie);
            }
        };
        activeTrackingMutation = addTask;

        addTask.setOnSucceeded(event -> {
            String feedback = addTask.getValue()
                    ? "Added to Watchlist."
                    : null;
            refreshTrackingActions(actions, movie, feedback, false);
            finishTrackingMutation(addTask);
        });
        addTask.setOnFailed(event -> {
            Throwable failure = addTask.getException();
            String error = failure instanceof StorageException
                    ? UiErrorMessages.trackingSaveFailure()
                    : UiErrorMessages.unexpectedTrackingFailure();
            refreshTrackingActions(actions, movie, error, true);
            finishTrackingMutation(addTask);
        });
        addTask.setOnCancelled(event -> finishTrackingMutation(addTask));

        executor.execute(addTask);
    }

    private static Label createMetadata(String label, String value) {
        Label metadata = new Label(label + ": " + value);
        metadata.getStyleClass().add("details-metadata");
        metadata.setWrapText(true);
        return metadata;
    }

    private void finishLoad(Task<MovieDetails> completedLoad) {
        if (activeLoad == completedLoad) {
            activeLoad = null;
        }
    }

    private void finishTrackingMutation(Task<Boolean> completedMutation) {
        if (activeTrackingMutation == completedMutation) {
            activeTrackingMutation = null;
        }
    }
}
