package movietracker.ui;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import movietracker.model.Movie;
import movietracker.model.TrackedMovie;
import movietracker.service.MovieTrackerApplicationService;

/**
 * Displays movies currently marked Watched in local tracking state.
 */
final class WatchedView extends VBox {

    private static final double POSTER_WIDTH = 60;
    private static final double POSTER_HEIGHT = 90;

    private final MovieTrackerApplicationService applicationService;
    private final ListView<TrackedMovie> movieList = new ListView<>();
    private final Label emptyState = new Label(
            "No watched movies yet. Mark a movie as Watched from its Details view.");

    WatchedView(MovieTrackerApplicationService applicationService,
                Consumer<Movie> movieSelectionHandler) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        Objects.requireNonNull(movieSelectionHandler, "movieSelectionHandler");

        getStyleClass().add("section");
        setPadding(new Insets(28));
        setSpacing(12);

        Label heading = new Label("Watched");
        heading.getStyleClass().add("section-heading");
        Label description = new Label("Movies you have watched, saved on this device.");
        description.getStyleClass().add("section-description");

        movieList.getStyleClass().addAll("collection-movies", "search-results");
        movieList.setCellFactory(ignored -> new WatchedCell(movieSelectionHandler));
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
        getChildren().addAll(heading, description, content);
    }

    /** Refreshes the view from the service's current in-memory Watched state. */
    void refresh() {
        movieList.getItems().setAll(applicationService.getWatched());
        boolean empty = movieList.getItems().isEmpty();
        movieList.setVisible(!empty);
        movieList.setManaged(!empty);
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
    }

    private static final class WatchedCell extends ListCell<TrackedMovie> {

        private final Consumer<Movie> selectionHandler;

        private WatchedCell(Consumer<Movie> selectionHandler) {
            this.selectionHandler = selectionHandler;
            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY
                        && event.getClickCount() == 1
                        && getItem() != null) {
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
            Label state = new Label("Watched");
            state.getStyleClass().add("collection-state");

            VBox text = new VBox(5, title, release, state);
            HBox.setHgrow(text, Priority.ALWAYS);
            HBox row = new HBox(
                    14,
                    new PosterView(movie.getPosterPath(), POSTER_WIDTH, POSTER_HEIGHT),
                    text);
            row.getStyleClass().addAll("movie-result", "collection-row");
            row.setAlignment(Pos.CENTER_LEFT);
            setText(null);
            setGraphic(row);
        }
    }
}
