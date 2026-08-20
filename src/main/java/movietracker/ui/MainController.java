package movietracker.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import movietracker.application.MovieCollectionManager;
import movietracker.application.MovieCollectionManager.MutationResult;
import movietracker.model.MovieInfo;
import movietracker.model.Movie;
import movietracker.model.MovieFactory;
import movietracker.model.WatchStatus;
import movietracker.service.MovieApiService;
import movietracker.service.MovieServiceException;

public class MainController {
    private enum View {
        SEARCH,
        DETAILS,
        WATCHLIST,
        WATCHED
    }

    private static final DateTimeFormatter RELEASE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final PseudoClass ERROR_STATE = PseudoClass.getPseudoClass("error");
    private static final PseudoClass SELECTED_STATE = PseudoClass.getPseudoClass("selected");
    private static final int RESULT_OVERVIEW_LIMIT = 180;
    private static final String SAVE_FAILURE_MESSAGE = "Your change is available for this session, "
            + "but it could not be saved. Check access to the data folder.";
    private static final String PERSISTENCE_DISABLED_MESSAGE = "Saved movie data could not be loaded. "
            + "The existing data file is preserved, and changes will only last for this session.";

    private final MovieApiService movieApiService;
    private final MovieCollectionManager movieCollectionManager;
    private final String startupWarning;
    private MovieInfo currentMovieDetails;
    private Integer loadingDetailsTmdbId;
    private long detailsRequestVersion;

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

    @FXML
    private Label storageFeedbackLabel;

    @FXML
    private Button searchNavButton;

    @FXML
    private Button watchlistNavButton;

    @FXML
    private Button watchedNavButton;

    public MainController(
            MovieApiService movieApiService,
            MovieCollectionManager movieCollectionManager,
            String startupWarning) {
        this.movieApiService = Objects.requireNonNull(movieApiService);
        this.movieCollectionManager = Objects.requireNonNull(movieCollectionManager);
        this.startupWarning = Objects.requireNonNull(startupWarning);
    }

    @FXML
    private void initialize() {
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        detailsLoadingIndicator.setVisible(false);
        detailsLoadingIndicator.setManaged(false);
        addToWatchlistButton.setDisable(true);
        showStorageFeedback(startupWarning);
        showView(View.SEARCH);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().strip();
        if (query.isEmpty()) {
            resultsBox.getChildren().clear();
            setFeedback(feedbackLabel, "Enter a movie title or keyword to search.", false);
            return;
        }

        setSearchInProgress(true);
        resultsBox.getChildren().clear();
        setFeedback(feedbackLabel, "Searching for movies...", false);

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
            setFeedback(
                    feedbackLabel,
                    MovieSearchMessages.forSearchFailure(searchTask.getException()),
                    true);
        });

        Thread searchThread = new Thread(searchTask, "movie-search");
        searchThread.setDaemon(true);
        searchThread.start();
    }

    private void showResults(String query, List<MovieInfo> movies) {
        resultsBox.getChildren().clear();
        if (movies.isEmpty()) {
            setFeedback(feedbackLabel, "No movies found for \"" + query + "\".", false);
            return;
        }

        setFeedback(
                feedbackLabel,
                "Found " + movies.size() + (movies.size() == 1 ? " movie." : " movies."),
                false);
        movies.forEach(movie -> resultsBox.getChildren().add(createResultButton(movie)));
    }

    private Button createResultButton(MovieInfo movie) {
        Label title = new Label(movie.title());
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.getStyleClass().add("movie-card-title");

        FlowPane metadata = new FlowPane(8.0, 8.0);
        metadata.getChildren().add(createMetadataLabel(
                movie.releaseDate() == null
                        ? "Release date unavailable"
                        : "Released " + RELEASE_DATE_FORMAT.format(movie.releaseDate()),
                "metadata-pill"));
        metadata.getChildren().add(createMetadataLabel(
                movie.externalRating() == null
                        ? "TMDB rating unavailable"
                        : String.format(Locale.ROOT, "TMDB %.1f/10", movie.externalRating()),
                "rating-pill"));

        VBox information = new VBox(8.0, title, metadata);
        HBox.setHgrow(information, Priority.ALWAYS);

        String overview = summarizeOverview(movie.overview());
        if (overview != null) {
            Label overviewLabel = new Label(overview);
            overviewLabel.setWrapText(true);
            overviewLabel.setMaxWidth(Double.MAX_VALUE);
            overviewLabel.getStyleClass().add("movie-card-overview");
            information.getChildren().add(overviewLabel);
        }

        HBox result = new HBox(16.0, createPosterPlaceholder(78.0, 110.0), information);
        result.setAlignment(Pos.TOP_LEFT);

        Button resultButton = new Button();
        resultButton.setGraphic(result);
        resultButton.setMaxWidth(Double.MAX_VALUE);
        resultButton.setAlignment(Pos.CENTER_LEFT);
        resultButton.getStyleClass().addAll("movie-card-button", "movie-card");
        resultButton.setAccessibleText("View details for " + movie.title());
        resultButton.setOnAction(event -> loadMovieDetails(movie));
        return resultButton;
    }

    private void loadMovieDetails(MovieInfo selectedMovie) {
        if (loadingDetailsTmdbId != null
                && loadingDetailsTmdbId == selectedMovie.tmdbId()) {
            showView(View.DETAILS);
            return;
        }

        long requestVersion = ++detailsRequestVersion;
        loadingDetailsTmdbId = selectedMovie.tmdbId();
        showView(View.DETAILS);
        setDetailsInProgress(true);
        clearDetails();
        currentMovieDetails = null;
        watchlistActionLabel.setText("");
        detailsTitleLabel.setText(selectedMovie.title());
        setFeedback(detailsFeedbackLabel, "Loading movie details...", false);

        Task<MovieInfo> detailsTask = new Task<>() {
            @Override
            protected MovieInfo call() throws MovieServiceException {
                return movieApiService.getMovieDetails(selectedMovie.tmdbId());
            }
        };

        detailsTask.setOnSucceeded(event -> {
            if (requestVersion != detailsRequestVersion) {
                return;
            }
            loadingDetailsTmdbId = null;
            setDetailsInProgress(false);
            showMovieDetails(detailsTask.getValue());
        });
        detailsTask.setOnFailed(event -> {
            if (requestVersion != detailsRequestVersion) {
                return;
            }
            loadingDetailsTmdbId = null;
            setDetailsInProgress(false);
            clearDetails();
            detailsTitleLabel.setText(selectedMovie.title());
            setFeedback(
                    detailsFeedbackLabel,
                    MovieSearchMessages.forDetailsFailure(detailsTask.getException()),
                    true);
        });

        Thread detailsThread = new Thread(detailsTask, "movie-details");
        detailsThread.setDaemon(true);
        detailsThread.start();
    }

    private void showMovieDetails(MovieInfo movie) {
        currentMovieDetails = movie;
        setFeedback(detailsFeedbackLabel, "", false);
        detailsTitleLabel.setText(movie.title());
        detailsReleaseDateLabel.setText(MovieDetailsText.releaseDate(movie));
        detailsRatingLabel.setText(MovieDetailsText.rating(movie));
        setDetailMetadataVisible(true);
        detailsOverviewLabel.setText(MovieDetailsText.overview(movie));
        updateAddToWatchlistState(movie.tmdbId());
    }

    private void clearDetails() {
        detailsTitleLabel.setText("");
        detailsReleaseDateLabel.setText("");
        detailsRatingLabel.setText("");
        setDetailMetadataVisible(false);
        detailsOverviewLabel.setText("");
        addToWatchlistButton.setDisable(true);
    }

    @FXML
    private void handleBackToResults() {
        showView(View.SEARCH);
    }

    @FXML
    private void handleShowSearch() {
        showView(View.SEARCH);
    }

    @FXML
    private void handleShowWatchlist() {
        refreshWatchlist();
        showView(View.WATCHLIST);
    }

    @FXML
    private void handleShowWatched() {
        refreshWatched();
        showView(View.WATCHED);
    }

    @FXML
    private void handleAddToWatchlist() {
        if (currentMovieDetails == null) {
            return;
        }

        Movie movie = MovieFactory.fromMovieInfo(currentMovieDetails);
        MutationResult result = movieCollectionManager.add(movie);
        if (result != MutationResult.NO_CHANGE) {
            watchlistActionLabel.setText("Added to your watchlist.");
            addToWatchlistButton.setDisable(true);
            showPersistenceResult(result);
        } else {
            updateAddToWatchlistState(movie.getTmdbId());
            addToWatchlistButton.setDisable(true);
        }
    }

    private void updateAddToWatchlistState(int tmdbId) {
        Optional<Movie> savedMovie = movieCollectionManager.findByTmdbId(tmdbId);
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
        List<Movie> watchlistMovies = movieCollectionManager.getWatchlistMovies();
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

    private VBox createWatchlistEntry(Movie movie) {
        HBox information = createSavedMovieInformation(movie, "Watchlist", false);

        Button watchedButton = new Button("Mark as Watched");
        watchedButton.getStyleClass().add("primary-button");
        watchedButton.setOnAction(event -> {
            MutationResult result = movieCollectionManager.markAsWatched(movie.getTmdbId());
            refreshWatchlist();
            refreshWatched();
            showPersistenceResult(result);
        });

        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("danger-button");
        removeButton.setOnAction(event -> {
            MutationResult result = movieCollectionManager.remove(movie.getTmdbId());
            refreshWatchlist();
            showPersistenceResult(result);
        });

        FlowPane actions = new FlowPane(8.0, 8.0, watchedButton, removeButton);
        VBox entry = new VBox(14.0, information, actions);
        entry.getStyleClass().addAll("movie-card", "saved-movie-card");
        return entry;
    }

    private void refreshWatched() {
        List<Movie> watchedMovies = movieCollectionManager.getWatchedMovies();
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

    private VBox createWatchedEntry(Movie movie) {
        HBox information = createSavedMovieInformation(movie, "Watched", true);
        VBox entry = new VBox(information);
        entry.getStyleClass().addAll("movie-card", "saved-movie-card");
        return entry;
    }

    private HBox createSavedMovieInformation(Movie movie, String status, boolean watched) {
        Label title = new Label(movie.getTitle());
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.getStyleClass().add("movie-card-title");

        FlowPane metadata = new FlowPane(8.0, 8.0);
        Label statusLabel = createMetadataLabel(status, "status-pill");
        if (watched) {
            statusLabel.getStyleClass().add("watched-status-pill");
        }
        metadata.getChildren().add(statusLabel);

        if (movie.getReleaseDate() != null) {
            metadata.getChildren().add(createMetadataLabel(
                    "Released " + RELEASE_DATE_FORMAT.format(movie.getReleaseDate()),
                    "metadata-pill"));
        }
        if (movie.getExternalRating() != null) {
            metadata.getChildren().add(createMetadataLabel(
                    String.format(Locale.ROOT, "TMDB %.1f/10", movie.getExternalRating()),
                    "rating-pill"));
        }

        VBox text = new VBox(8.0, title, metadata);
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox information = new HBox(16.0, createPosterPlaceholder(70.0, 98.0), text);
        information.setAlignment(Pos.TOP_LEFT);
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

    private void showPersistenceResult(MutationResult result) {
        if (result == MutationResult.SAVE_FAILED) {
            showStorageFeedback(SAVE_FAILURE_MESSAGE);
        } else if (result == MutationResult.PERSISTENCE_DISABLED) {
            showStorageFeedback(PERSISTENCE_DISABLED_MESSAGE);
        } else if (result == MutationResult.SUCCESS) {
            showStorageFeedback("");
        }
    }

    private void showStorageFeedback(String message) {
        boolean hasMessage = !message.isBlank();
        storageFeedbackLabel.setText(message);
        storageFeedbackLabel.setVisible(hasMessage);
        storageFeedbackLabel.setManaged(hasMessage);
    }

    private void showView(View view) {
        setViewState(searchView, view == View.SEARCH);
        setViewState(detailsView, view == View.DETAILS);
        setViewState(watchlistView, view == View.WATCHLIST);
        setViewState(watchedView, view == View.WATCHED);
        updateNavigationState(
                searchNavButton,
                view == View.SEARCH || view == View.DETAILS,
                "Search");
        updateNavigationState(watchlistNavButton, view == View.WATCHLIST, "Watchlist");
        updateNavigationState(watchedNavButton, view == View.WATCHED, "Watched");
    }

    private void setViewState(VBox view, boolean shown) {
        view.setVisible(shown);
        view.setManaged(shown);
    }

    private Label createMetadataLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private StackPane createPosterPlaceholder(double width, double height) {
        Label placeholderText = new Label("POSTER");
        placeholderText.getStyleClass().add("poster-placeholder-text");

        StackPane placeholder = new StackPane(placeholderText);
        placeholder.setMinSize(width, height);
        placeholder.setPrefSize(width, height);
        placeholder.setMaxSize(width, height);
        placeholder.getStyleClass().add("poster-placeholder");
        return placeholder;
    }

    private String summarizeOverview(String overview) {
        if (overview == null || overview.isBlank()) {
            return null;
        }

        String normalized = overview.strip();
        if (normalized.length() <= RESULT_OVERVIEW_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, RESULT_OVERVIEW_LIMIT - 1).stripTrailing() + "…";
    }

    private void setFeedback(Label label, String message, boolean error) {
        label.setText(message);
        label.pseudoClassStateChanged(ERROR_STATE, error);
    }

    private void setDetailMetadataVisible(boolean visible) {
        detailsReleaseDateLabel.setVisible(visible);
        detailsReleaseDateLabel.setManaged(visible);
        detailsRatingLabel.setVisible(visible);
        detailsRatingLabel.setManaged(visible);
    }

    private void updateNavigationState(Button button, boolean selected, String name) {
        button.pseudoClassStateChanged(SELECTED_STATE, selected);
        button.setAccessibleText(selected ? name + ", current view" : name);
    }
}
