package movietracker.ui;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import movietracker.api.TmdbErrorCategory;
import movietracker.api.TmdbException;
import movietracker.model.Movie;
import movietracker.service.MovieTrackerApplicationService;

/**
 * Search section that performs TMDB lookup through the application service.
 */
final class SearchView extends VBox {

    private final MovieTrackerApplicationService applicationService;
    private final Executor executor;
    private final TextField searchField = new TextField();
    private final Button searchButton = new Button("Search");
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final ListView<Movie> resultsList = new ListView<>();
    private final Label stateMessage = new Label("Search results will appear here.");

    private Task<List<Movie>> activeSearch;

    SearchView(MovieTrackerApplicationService applicationService, Executor executor) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.executor = Objects.requireNonNull(executor, "executor");

        getStyleClass().add("section");
        setPadding(new Insets(28));
        setSpacing(12);

        Label heading = new Label("Search");
        heading.getStyleClass().add("section-heading");
        Label description = new Label("Find a movie by title.");
        description.getStyleClass().add("section-description");

        configureSearchControls();
        StackPane resultsArea = configureResultsArea();
        VBox.setVgrow(resultsArea, Priority.ALWAYS);

        getChildren().addAll(heading, description, createSearchControls(), resultsArea);
    }

    void cancelActiveSearch() {
        if (activeSearch != null) {
            activeSearch.cancel(true);
        }
    }

    private void configureSearchControls() {
        searchField.setPromptText("Search movies");
        searchField.setAccessibleText("Movie title");
        searchField.setOnAction(event -> submitSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchButton.setDefaultButton(true);
        searchButton.setOnAction(event -> submitSearch());

        progressIndicator.setMaxSize(24, 24);
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        progressIndicator.getStyleClass().add("search-progress");
    }

    private HBox createSearchControls() {
        HBox controls = new HBox(10, searchField, searchButton, progressIndicator);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setMaxWidth(800);
        return controls;
    }

    private StackPane configureResultsArea() {
        resultsList.getStyleClass().add("search-results");
        resultsList.setCellFactory(ignored -> new MovieResultCell());
        resultsList.setVisible(false);
        resultsList.setManaged(false);

        stateMessage.getStyleClass().add("empty-state");
        stateMessage.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        stateMessage.setAlignment(Pos.CENTER);
        stateMessage.setWrapText(true);

        StackPane resultsArea = new StackPane(resultsList, stateMessage);
        resultsArea.setMinHeight(180);
        return resultsArea;
    }

    private void submitSearch() {
        if (activeSearch != null && !activeSearch.isDone()) {
            return;
        }

        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        if (query.isEmpty()) {
            showMessage("Enter a movie title to search.");
            return;
        }

        Task<List<Movie>> searchTask = new Task<>() {
            @Override
            protected List<Movie> call() throws TmdbException {
                return applicationService.searchMovies(query);
            }
        };
        activeSearch = searchTask;
        setSearching(true);
        showMessage("Searching TMDB…");

        searchTask.setOnSucceeded(event -> {
            List<Movie> movies = searchTask.getValue();
            if (movies.isEmpty()) {
                showMessage("No movies found for \"" + query + "\".");
            } else {
                resultsList.getItems().setAll(movies);
                showResults();
            }
            finishSearch(searchTask);
        });
        searchTask.setOnFailed(event -> {
            Throwable failure = searchTask.getException();
            if (failure instanceof TmdbException tmdbException) {
                showMessage(SearchErrorMessages.forCategory(tmdbException.getCategory()));
            } else {
                showMessage("Movie search failed unexpectedly. Try again.");
            }
            finishSearch(searchTask);
        });
        searchTask.setOnCancelled(event -> {
            showMessage(SearchErrorMessages.forCategory(TmdbErrorCategory.INTERRUPTED));
            finishSearch(searchTask);
        });

        executor.execute(searchTask);
    }

    private void setSearching(boolean searching) {
        searchField.setDisable(searching);
        searchButton.setDisable(searching);
        progressIndicator.setManaged(searching);
        progressIndicator.setVisible(searching);
    }

    private void finishSearch(Task<List<Movie>> completedSearch) {
        if (activeSearch == completedSearch) {
            activeSearch = null;
            setSearching(false);
        }
    }

    private void showMessage(String message) {
        resultsList.getItems().clear();
        resultsList.setManaged(false);
        resultsList.setVisible(false);
        stateMessage.setText(message);
        stateMessage.setManaged(true);
        stateMessage.setVisible(true);
    }

    private void showResults() {
        stateMessage.setManaged(false);
        stateMessage.setVisible(false);
        resultsList.setManaged(true);
        resultsList.setVisible(true);
    }

    private static final class MovieResultCell extends ListCell<Movie> {

        @Override
        protected void updateItem(Movie movie, boolean empty) {
            super.updateItem(movie, empty);
            if (empty || movie == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            Label title = new Label(movie.getTitle());
            title.getStyleClass().add("movie-title");
            Label release = new Label(movie.getReleaseDate()
                    .map(MovieResultCell::formatReleaseYear)
                    .orElse("Release date unavailable"));
            release.getStyleClass().add("movie-release");

            VBox content = new VBox(4, title, release);
            content.getStyleClass().add("movie-result");
            setGraphic(content);
            setText(null);
        }

        private static String formatReleaseYear(LocalDate releaseDate) {
            return Integer.toString(releaseDate.getYear());
        }
    }
}
