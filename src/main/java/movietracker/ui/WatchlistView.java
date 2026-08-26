package movietracker.ui;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import movietracker.model.Movie;
import movietracker.model.TrackedMovie;
import movietracker.service.MovieTrackerApplicationService;
import movietracker.storage.StorageException;

/**
 * Displays locally persisted Watchlist movies and coordinates Watchlist removal.
 */
final class WatchlistView extends VBox {

    private static final double POSTER_WIDTH = 60;
    private static final double POSTER_HEIGHT = 90;

    private final MovieTrackerApplicationService applicationService;
    private final Executor executor;
    private final ListView<TrackedMovie> movieList = new ListView<>();
    private final Label emptyState = new Label(
            "Your Watchlist is empty. Add a movie from its Details view.");
    private final HBox operationState = new HBox(8);
    private final ProgressIndicator operationProgress = new ProgressIndicator();
    private final Label operationMessage = new Label();

    private Task<Boolean> activeRemoval;

    WatchlistView(MovieTrackerApplicationService applicationService,
                  Executor executor,
                  Consumer<Movie> movieSelectionHandler) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(movieSelectionHandler, "movieSelectionHandler");

        getStyleClass().add("section");
        setPadding(new Insets(28));
        setSpacing(12);

        Label heading = new Label("Watchlist");
        heading.getStyleClass().add("section-heading");
        Label description = new Label("Movies you want to watch, saved on this device.");
        description.getStyleClass().add("section-description");

        operationProgress.setMaxSize(20, 20);
        operationMessage.setWrapText(true);
        operationState.setAlignment(Pos.CENTER_LEFT);
        operationState.getChildren().addAll(operationProgress, operationMessage);
        setOperationStateVisible(false);

        movieList.getStyleClass().addAll("collection-movies", "search-results");
        movieList.setCellFactory(ignored -> new WatchlistCell(
                movieSelectionHandler, this::removeFromWatchlist));
        movieList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                TrackedMovie selected = movieList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    movieSelectionHandler.accept(selected.getMovie());
                }
            }
        });

        emptyState.getStyleClass().add("empty-state");
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setWrapText(true);
        emptyState.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane content = new StackPane(movieList, emptyState);
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().addAll(heading, description, operationState, content);
    }

    /** Refreshes the view from the service's current in-memory tracking state. */
    void refresh() {
        setOperationStateVisible(false);
        movieList.getItems().setAll(applicationService.getWatchlist());
        boolean empty = movieList.getItems().isEmpty();
        movieList.setVisible(!empty);
        movieList.setManaged(!empty);
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
    }

    /** Cancels an in-flight removal while the application is shutting down. */
    void cancelActiveRemoval() {
        if (activeRemoval != null) {
            activeRemoval.cancel(true);
        }
    }

    private void removeFromWatchlist(TrackedMovie trackedMovie) {
        if (activeRemoval != null && !activeRemoval.isDone()) {
            return;
        }

        movieList.setDisable(true);
        showOperationState("Removing " + trackedMovie.getMovie().getTitle() + "…", true, false);

        Task<Boolean> removalTask = new Task<>() {
            @Override
            protected Boolean call() throws StorageException {
                return applicationService.removeTrackedMovie(
                        trackedMovie.getMovie().getTmdbId());
            }
        };
        activeRemoval = removalTask;

        removalTask.setOnSucceeded(event -> {
            finishRemoval(removalTask);
            refresh();
            String message = removalTask.getValue()
                    ? "Removed from Watchlist."
                    : "That movie is no longer in the Watchlist.";
            showOperationState(message, false, false);
        });
        removalTask.setOnFailed(event -> {
            Throwable failure = removalTask.getException();
            finishRemoval(removalTask);
            String message = failure instanceof StorageException
                    ? UiErrorMessages.trackingSaveFailure()
                    : UiErrorMessages.unexpectedTrackingFailure();
            showOperationState(message, false, true);
        });
        removalTask.setOnCancelled(event -> finishRemoval(removalTask));

        executor.execute(removalTask);
    }

    private void finishRemoval(Task<Boolean> completedRemoval) {
        if (activeRemoval == completedRemoval) {
            activeRemoval = null;
        }
        movieList.setDisable(false);
    }

    private void showOperationState(String message, boolean showProgress, boolean error) {
        operationProgress.setVisible(showProgress);
        operationProgress.setManaged(showProgress);
        operationMessage.setText(message);
        operationMessage.getStyleClass().removeAll("tracking-feedback", "tracking-error");
        operationMessage.getStyleClass().add(error ? "tracking-error" : "tracking-feedback");
        setOperationStateVisible(true);
    }

    private void setOperationStateVisible(boolean visible) {
        operationState.setVisible(visible);
        operationState.setManaged(visible);
    }

    private static final class WatchlistCell extends ListCell<TrackedMovie> {

        private final Consumer<Movie> selectionHandler;
        private final Consumer<TrackedMovie> removalHandler;

        private WatchlistCell(Consumer<Movie> selectionHandler,
                              Consumer<TrackedMovie> removalHandler) {
            this.selectionHandler = selectionHandler;
            this.removalHandler = removalHandler;
            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 1
                        && getItem() != null
                        && !originatedFromButton(event.getTarget())) {
                    selectionHandler.accept(getItem().getMovie());
                }
            });
        }

        @Override
        protected void updateItem(TrackedMovie trackedMovie, boolean empty) {
            super.updateItem(trackedMovie, empty);
            if (empty || trackedMovie == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Movie movie = trackedMovie.getMovie();
            Label title = new Label(movie.getTitle());
            title.getStyleClass().add("movie-title");
            title.setWrapText(true);
            Label release = new Label("Release: " + movie.getReleaseDate()
                    .map(LocalDate::toString)
                    .orElse("Unavailable"));
            release.getStyleClass().add("movie-release");
            Label state = new Label("Watchlist");
            state.getStyleClass().add("collection-state");
            VBox text = new VBox(5, title, release, state);
            HBox.setHgrow(text, Priority.ALWAYS);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button remove = new Button("Remove");
            remove.getStyleClass().add("remove-action");
            remove.setOnAction(event -> removalHandler.accept(trackedMovie));

            HBox row = new HBox(
                    14,
                    new PosterView(movie.getPosterPath(), POSTER_WIDTH, POSTER_HEIGHT),
                    text,
                    spacer,
                    remove);
            row.getStyleClass().addAll("movie-result", "collection-row");
            row.setAlignment(Pos.CENTER_LEFT);
            setText(null);
            setGraphic(row);
        }

        private boolean originatedFromButton(Object target) {
            if (!(target instanceof Node node)) {
                return false;
            }
            Node current = node;
            while (current != null && current != this) {
                if (current instanceof Button) {
                    return true;
                }
                current = current.getParent();
            }
            return false;
        }
    }
}
