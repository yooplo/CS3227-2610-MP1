package movietracker.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import movietracker.model.MovieInfo;
import movietracker.model.Movie;
import movietracker.model.MovieCollection;
import movietracker.model.MovieFactory;
import movietracker.model.WatchStatus;
import movietracker.service.MovieApiService;
import movietracker.service.MovieServiceException;

public class MainController {
    private static final DateTimeFormatter RELEASE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final MovieApiService movieApiService;
    private final MovieCollection movieCollection;
    private MovieInfo currentMovieDetails;

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

    @FXML
    private Button addToWatchlistButton;

    @FXML
    private Label watchlistActionLabel;

    @FXML
    private VBox watchlistView;

    @FXML
    private VBox watchlistBox;

    @FXML
    private Label watchlistFeedbackLabel;

    @FXML
    private VBox watchedView;

    @FXML
    private VBox watchedBox;

    @FXML
    private Label watchedFeedbackLabel;

    public MainController(MovieApiService movieApiService, MovieCollection movieCollection) {
        this.movieApiService = Objects.requireNonNull(movieApiService);
        this.movieCollection = Objects.requireNonNull(movieCollection);
    }

    @FXML
    private void initialize() {
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        detailsLoadingIndicator.setVisible(false);
        detailsLoadingIndicator.setManaged(false);
        addToWatchlistButton.setDisable(true);
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
        currentMovieDetails = null;
        watchlistActionLabel.setText("");
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
        currentMovieDetails = movie;
        detailsFeedbackLabel.setText("");
        detailsTitleLabel.setText(movie.title());
        detailsReleaseDateLabel.setText(MovieDetailsText.releaseDate(movie));
        detailsRatingLabel.setText(MovieDetailsText.rating(movie));
        detailsOverviewLabel.setText(MovieDetailsText.overview(movie));
        updateAddToWatchlistState(movie.tmdbId());
    }

    private void clearDetails() {
        detailsTitleLabel.setText("");
        detailsReleaseDateLabel.setText("");
        detailsRatingLabel.setText("");
        detailsOverviewLabel.setText("");
        addToWatchlistButton.setDisable(true);
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
        watchlistView.setVisible(false);
        watchlistView.setManaged(false);
        watchedView.setVisible(false);
        watchedView.setManaged(false);
    }

    private void showDetailsView() {
        searchView.setVisible(false);
        searchView.setManaged(false);
        detailsView.setVisible(true);
        detailsView.setManaged(true);
        watchlistView.setVisible(false);
        watchlistView.setManaged(false);
        watchedView.setVisible(false);
        watchedView.setManaged(false);
    }

    @FXML
    private void handleShowSearch() {
        showSearchView();
    }

    @FXML
    private void handleShowWatchlist() {
        refreshWatchlist();
        searchView.setVisible(false);
        searchView.setManaged(false);
        detailsView.setVisible(false);
        detailsView.setManaged(false);
        watchlistView.setVisible(true);
        watchlistView.setManaged(true);
        watchedView.setVisible(false);
        watchedView.setManaged(false);
    }

    @FXML
    private void handleShowWatched() {
        refreshWatched();
        searchView.setVisible(false);
        searchView.setManaged(false);
        detailsView.setVisible(false);
        detailsView.setManaged(false);
        watchlistView.setVisible(false);
        watchlistView.setManaged(false);
        watchedView.setVisible(true);
        watchedView.setManaged(true);
    }

    @FXML
    private void handleAddToWatchlist() {
        if (currentMovieDetails == null) {
            return;
        }

        Movie movie = MovieFactory.fromMovieInfo(currentMovieDetails);
        if (movieCollection.add(movie)) {
            watchlistActionLabel.setText("Added to your watchlist.");
            addToWatchlistButton.setDisable(true);
        } else {
            updateAddToWatchlistState(movie.getTmdbId());
            addToWatchlistButton.setDisable(true);
        }
    }

    private void updateAddToWatchlistState(int tmdbId) {
        Optional<Movie> savedMovie = movieCollection.findByTmdbId(tmdbId);
        if (savedMovie.isEmpty()) {
            addToWatchlistButton.setDisable(false);
            watchlistActionLabel.setText("");
            return;
        }

        addToWatchlistButton.setDisable(true);
        watchlistActionLabel.setText(savedMovie.orElseThrow().getWatchStatus() == WatchStatus.WATCHED
                ? "This movie is already in your collection and already watched."
                : "This movie is already in your watchlist.");
    }

    private void refreshWatchlist() {
        List<Movie> watchlistMovies = movieCollection.getWatchlistMovies();
        watchlistBox.getChildren().clear();

        if (watchlistMovies.isEmpty()) {
            watchlistFeedbackLabel.setText("Your watchlist is empty.");
            return;
        }

        watchlistFeedbackLabel.setText(
                watchlistMovies.size() + (watchlistMovies.size() == 1
                        ? " movie in your watchlist."
                        : " movies in your watchlist."));
        watchlistMovies.forEach(movie -> watchlistBox.getChildren().add(createWatchlistEntry(movie)));
    }

    private HBox createWatchlistEntry(Movie movie) {
        VBox information = createSavedMovieInformation(movie);

        Button watchedButton = new Button("Mark as Watched");
        watchedButton.setOnAction(event -> {
            movieCollection.markAsWatched(movie.getTmdbId());
            refreshWatchlist();
            refreshWatched();
        });

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(event -> {
            movieCollection.remove(movie.getTmdbId());
            refreshWatchlist();
        });

        HBox entry = new HBox(12.0, information, watchedButton, removeButton);
        entry.setAlignment(Pos.CENTER_LEFT);
        entry.setStyle("-fx-padding: 12px; -fx-background-color: #f3f4f6; "
                + "-fx-background-radius: 6px;");
        return entry;
    }

    private void refreshWatched() {
        List<Movie> watchedMovies = movieCollection.getWatchedMovies();
        watchedBox.getChildren().clear();

        if (watchedMovies.isEmpty()) {
            watchedFeedbackLabel.setText("You have not marked any movies as watched.");
            return;
        }

        watchedFeedbackLabel.setText(
                watchedMovies.size() + (watchedMovies.size() == 1
                        ? " watched movie."
                        : " watched movies."));
        watchedMovies.forEach(movie -> watchedBox.getChildren().add(createWatchedEntry(movie)));
    }

    private HBox createWatchedEntry(Movie movie) {
        VBox information = createSavedMovieInformation(movie);
        HBox entry = new HBox(12.0, information);
        entry.setAlignment(Pos.CENTER_LEFT);
        entry.setStyle("-fx-padding: 12px; -fx-background-color: #f3f4f6; "
                + "-fx-background-radius: 6px;");
        return entry;
    }

    private VBox createSavedMovieInformation(Movie movie) {
        Label title = new Label(movie.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        VBox information = new VBox(4.0, title);
        HBox.setHgrow(information, Priority.ALWAYS);

        if (movie.getReleaseDate() != null) {
            information.getChildren().add(new Label(
                    "Release date: " + RELEASE_DATE_FORMAT.format(movie.getReleaseDate())));
        }
        if (movie.getExternalRating() != null) {
            information.getChildren().add(new Label(String.format(
                    Locale.ROOT,
                    "TMDB rating: %.1f/10",
                    movie.getExternalRating())));
        }

        return information;
    }

    private void setDetailsInProgress(boolean inProgress) {
        backButton.setDisable(inProgress);
        detailsLoadingIndicator.setVisible(inProgress);
        detailsLoadingIndicator.setManaged(inProgress);
        if (inProgress) {
            addToWatchlistButton.setDisable(true);
        }
    }

    private void setSearchInProgress(boolean inProgress) {
        searchField.setDisable(inProgress);
        searchButton.setDisable(inProgress);
        loadingIndicator.setVisible(inProgress);
        loadingIndicator.setManaged(inProgress);
    }
}
