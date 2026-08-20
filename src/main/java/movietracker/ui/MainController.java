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

    public MainController(MovieApiService movieApiService) {
        this.movieApiService = Objects.requireNonNull(movieApiService);
    }

    @FXML
    private void initialize() {
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
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
            feedbackLabel.setText(MovieSearchMessages.forFailure(searchTask.getException()));
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
        movies.forEach(movie -> resultsBox.getChildren().add(createResult(movie)));
    }

    private VBox createResult(MovieInfo movie) {
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
        return result;
    }

    private void setSearchInProgress(boolean inProgress) {
        searchField.setDisable(inProgress);
        searchButton.setDisable(inProgress);
        loadingIndicator.setVisible(inProgress);
        loadingIndicator.setManaged(inProgress);
    }
}
