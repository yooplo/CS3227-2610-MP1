package movietracker.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import movietracker.model.MovieInfo;
import movietracker.service.MovieApiService;
import movietracker.service.MovieServiceException;

public class MainController {
    private static final DateTimeFormatter RELEASE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final MovieApiService movieApiService;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label feedbackLabel;

    @FXML
    private VBox resultsBox;

    @FXML
    private VBox searchView;

    @FXML
    private VBox detailsView;

    @FXML
    private Button backButton;

    @FXML
    private ProgressIndicator detailsLoadingIndicator;

    @FXML
    private Label detailsFeedbackLabel;

    @FXML
    private Label detailsTitleLabel;

    @FXML
    private Label detailsReleaseDateLabel;

    @FXML
    private Label detailsRatingLabel;

    @FXML
    private Label detailsOverviewLabel;

    public MainController(MovieApiService movieApiService) {
        this.movieApiService = Objects.requireNonNull(movieApiService);
    }

    @FXML
    private void initialize() {
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        detailsLoadingIndicator.setVisible(false);
        detailsLoadingIndicator.setManaged(false);
        showSearchView();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().strip();
        if (query.isEmpty()) {
            resultsBox.getChildren().clear();
            feedbackLabel.setText("Enter a movie title or keyword to search.");
            return;
        }

        setSearchInProgress(true);
        resultsBox.getChildren().clear();
        feedbackLabel.setText("Searching for movies...");

        Task<List<MovieInfo>> searchTask = new Task<>() {
            @Override
            protected List<MovieInfo> call() throws MovieServiceException {
                return movieApiService.searchMovies(query);
            }
        };

        searchTask.setOnSucceeded(event -> {
            setSearchInProgress(false);
            showResults(query, searchTask.getValue());
        });
        searchTask.setOnFailed(event -> {
            setSearchInProgress(false);
            resultsBox.getChildren().clear();
            feedbackLabel.setText(MovieSearchMessages.forSearchFailure(searchTask.getException()));
        });

        Thread searchThread = new Thread(searchTask, "movie-search");
        searchThread.setDaemon(true);
        searchThread.start();
    }

    private void showResults(String query, List<MovieInfo> movies) {
        resultsBox.getChildren().clear();
        if (movies.isEmpty()) {
            feedbackLabel.setText("No movies found for \"" + query + "\".");
            return;
        }

        feedbackLabel.setText("Found " + movies.size() + (movies.size() == 1 ? " movie." : " movies."));
        movies.forEach(movie -> resultsBox.getChildren().add(createResultButton(movie)));
    }

    private Button createResultButton(MovieInfo movie) {
        Label title = new Label(movie.title());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox result = new VBox(4.0, title);
        result.setStyle("-fx-padding: 12px; -fx-background-color: #f3f4f6; "
                + "-fx-background-radius: 6px;");

        if (movie.releaseDate() != null) {
            result.getChildren().add(new Label(
                    "Release date: " + RELEASE_DATE_FORMAT.format(movie.releaseDate())));
        }
        if (movie.externalRating() != null) {
            result.getChildren().add(new Label(String.format(
                    Locale.ROOT,
                    "TMDB rating: %.1f/10",
                    movie.externalRating())));
        }

        Button resultButton = new Button();
        resultButton.setGraphic(result);
        resultButton.setMaxWidth(Double.MAX_VALUE);
        resultButton.setAlignment(Pos.CENTER_LEFT);
        resultButton.setStyle("-fx-padding: 0; -fx-background-color: transparent;");
        resultButton.setOnAction(event -> loadMovieDetails(movie));
        return resultButton;
    }

    private void loadMovieDetails(MovieInfo selectedMovie) {
        showDetailsView();
        setDetailsInProgress(true);
        clearDetails();
        detailsTitleLabel.setText(selectedMovie.title());
        detailsFeedbackLabel.setText("Loading movie details...");

        Task<MovieInfo> detailsTask = new Task<>() {
            @Override
            protected MovieInfo call() throws MovieServiceException {
                return movieApiService.getMovieDetails(selectedMovie.tmdbId());
            }
        };

        detailsTask.setOnSucceeded(event -> {
            setDetailsInProgress(false);
            showMovieDetails(detailsTask.getValue());
        });
        detailsTask.setOnFailed(event -> {
            setDetailsInProgress(false);
            clearDetails();
            detailsTitleLabel.setText(selectedMovie.title());
            detailsFeedbackLabel.setText(
                    MovieSearchMessages.forDetailsFailure(detailsTask.getException()));
        });

        Thread detailsThread = new Thread(detailsTask, "movie-details");
        detailsThread.setDaemon(true);
        detailsThread.start();
    }

    private void showMovieDetails(MovieInfo movie) {
        detailsFeedbackLabel.setText("");
        detailsTitleLabel.setText(movie.title());
        detailsReleaseDateLabel.setText(MovieDetailsText.releaseDate(movie));
        detailsRatingLabel.setText(MovieDetailsText.rating(movie));
        detailsOverviewLabel.setText(MovieDetailsText.overview(movie));
    }

    private void clearDetails() {
        detailsTitleLabel.setText("");
        detailsReleaseDateLabel.setText("");
        detailsRatingLabel.setText("");
        detailsOverviewLabel.setText("");
    }

    @FXML
    private void handleBackToResults() {
        showSearchView();
    }

    private void showSearchView() {
        searchView.setVisible(true);
        searchView.setManaged(true);
        detailsView.setVisible(false);
        detailsView.setManaged(false);
    }

    private void showDetailsView() {
        searchView.setVisible(false);
        searchView.setManaged(false);
        detailsView.setVisible(true);
        detailsView.setManaged(true);
    }

    private void setDetailsInProgress(boolean inProgress) {
        backButton.setDisable(inProgress);
        detailsLoadingIndicator.setVisible(inProgress);
        detailsLoadingIndicator.setManaged(inProgress);
    }

    private void setSearchInProgress(boolean inProgress) {
        searchField.setDisable(inProgress);
        searchButton.setDisable(inProgress);
        loadingIndicator.setVisible(inProgress);
        loadingIndicator.setManaged(inProgress);
    }
}
