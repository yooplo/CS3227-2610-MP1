package movietracker.ui;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import movietracker.model.Movie;
import movietracker.service.MovieTrackerApplicationService;

/**
 * Main single-window shell and navigation for Movie Tracker.
 */
public final class MainWindow extends BorderPane {

    private static final String ACTIVE_NAVIGATION_STYLE = "nav-button-active";

    private final StackPane contentArea = new StackPane();
    private final Map<Section, Button> navigationButtons = new EnumMap<>(Section.class);
    private final Map<Section, Node> sectionViews = new EnumMap<>(Section.class);
    private final SearchView searchView;
    private final WatchlistView watchlistView;
    private final WatchedView watchedView;
    private final MovieTrackerApplicationService applicationService;
    private final Executor applicationExecutor;
    private MovieDetailsView detailsView;
    private Section detailsOrigin;

    /** Creates the application shell and section views. */
    public MainWindow(MovieTrackerApplicationService applicationService,
                      Executor applicationExecutor) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.applicationExecutor = Objects.requireNonNull(
                applicationExecutor, "applicationExecutor");
        getStyleClass().add("main-window");

        searchView = new SearchView(
                applicationService, applicationExecutor,
                movie -> showMovieDetails(movie, Section.SEARCH));
        watchlistView = new WatchlistView(
                applicationService, applicationExecutor,
                movie -> showMovieDetails(movie, Section.WATCHLIST));
        watchedView = new WatchedView(
                applicationService, applicationExecutor,
                movie -> showMovieDetails(movie, Section.WATCHED));
        sectionViews.put(Section.SEARCH, searchView);
        sectionViews.put(Section.WATCHLIST, watchlistView);
        sectionViews.put(Section.WATCHED, watchedView);
        sectionViews.put(Section.ABOUT, new AboutView());

        setLeft(createNavigation());
        setCenter(contentArea);
        showSection(Section.SEARCH);
    }

    /**
     * Cancels UI work when the application is stopping.
     */
    public void close() {
        searchView.cancelActiveSearch();
        watchlistView.cancelActiveRemoval();
        watchedView.cancelActiveRemoval();
        closeDetailsView();
    }

    private VBox createNavigation() {
        Label title = new Label("Movie Tracker");
        title.getStyleClass().add("app-title");

        VBox navigation = new VBox(8, title);
        navigation.getStyleClass().add("navigation");
        navigation.setPrefWidth(184);
        navigation.setMinWidth(156);

        for (Section section : Section.values()) {
            Button button = new Button(section.label);
            button.getStyleClass().add("nav-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setOnAction(event -> showSection(section));
            navigationButtons.put(section, button);
            navigation.getChildren().add(button);
        }
        return navigation;
    }

    private void showSection(Section section) {
        closeDetailsView();
        if (section == Section.WATCHLIST) {
            watchlistView.refresh();
        } else if (section == Section.WATCHED) {
            watchedView.refresh();
        }
        contentArea.getChildren().setAll(sectionViews.get(section));
        navigationButtons.forEach((candidate, button) -> {
            button.getStyleClass().remove(ACTIVE_NAVIGATION_STYLE);
            if (candidate == section) {
                button.getStyleClass().add(ACTIVE_NAVIGATION_STYLE);
            }
        });
    }

    private void showMovieDetails(Movie movie, Section origin) {
        closeDetailsView();
        detailsOrigin = origin;
        detailsView = new MovieDetailsView(
                movie, applicationService, applicationExecutor, origin.label,
                this::returnFromDetails);
        contentArea.getChildren().setAll(detailsView);
    }

    private void returnFromDetails() {
        Section returnSection = detailsOrigin == null ? Section.SEARCH : detailsOrigin;
        showSection(returnSection);
    }

    private void closeDetailsView() {
        if (detailsView != null) {
            detailsView.cancelActiveLoad();
            detailsView = null;
        }
        detailsOrigin = null;
    }

    private enum Section {
        SEARCH("Search"),
        WATCHLIST("Watchlist"),
        WATCHED("Watched"),
        ABOUT("About");

        private final String label;

        Section(String label) {
            this.label = label;
        }
    }
}
