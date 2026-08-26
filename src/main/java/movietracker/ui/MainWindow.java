package movietracker.ui;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
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
    private final MovieTrackerApplicationService applicationService;
    private final Executor tmdbExecutor;
    private MovieDetailsView detailsView;

    /**
     * Creates the application shell with placeholder section views.
     */
    public MainWindow(MovieTrackerApplicationService applicationService, Executor tmdbExecutor) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.tmdbExecutor = Objects.requireNonNull(tmdbExecutor, "tmdbExecutor");
        getStyleClass().add("main-window");

        searchView = new SearchView(applicationService, tmdbExecutor, this::showMovieDetails);
        sectionViews.put(Section.SEARCH, searchView);
        sectionViews.put(Section.WATCHLIST, createCollectionView(
                "Watchlist", "Movies saved to your Watchlist will appear here."));
        sectionViews.put(Section.WATCHED, createCollectionView(
                "Watched", "Movies marked as Watched will appear here."));

        setLeft(createNavigation());
        setCenter(contentArea);
        showSection(Section.SEARCH);
    }

    /**
     * Cancels UI work when the application is stopping.
     */
    public void close() {
        searchView.cancelActiveSearch();
        closeDetailsView();
    }

    private VBox createNavigation() {
        Label title = new Label("Movie Tracker");
        title.getStyleClass().add("app-title");

        VBox navigation = new VBox(8, title);
        navigation.getStyleClass().add("navigation");
        navigation.setPrefWidth(200);
        navigation.setMinWidth(170);

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

    private Node createCollectionView(String headingText, String emptyStateText) {
        Label heading = createHeading(headingText);
        Label description = new Label("Your locally tracked movies.");
        description.getStyleClass().add("section-description");

        Label placeholder = createEmptyState(emptyStateText);
        VBox.setVgrow(placeholder, Priority.ALWAYS);

        return createSection(heading, description, placeholder);
    }

    private static VBox createSection(Node... children) {
        VBox section = new VBox(12, children);
        section.getStyleClass().add("section");
        section.setPadding(new Insets(28));
        return section;
    }

    private static Label createHeading(String text) {
        Label heading = new Label(text);
        heading.getStyleClass().add("section-heading");
        return heading;
    }

    private static Label createEmptyState(String text) {
        Label placeholder = new Label(text);
        placeholder.getStyleClass().add("empty-state");
        placeholder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setWrapText(true);
        return placeholder;
    }

    private void showSection(Section section) {
        closeDetailsView();
        contentArea.getChildren().setAll(sectionViews.get(section));
        navigationButtons.forEach((candidate, button) -> {
            button.getStyleClass().remove(ACTIVE_NAVIGATION_STYLE);
            if (candidate == section) {
                button.getStyleClass().add(ACTIVE_NAVIGATION_STYLE);
            }
        });
    }

    private void showMovieDetails(Movie movie) {
        closeDetailsView();
        detailsView = new MovieDetailsView(
                movie, applicationService, tmdbExecutor, this::returnToSearch);
        contentArea.getChildren().setAll(detailsView);
    }

    private void returnToSearch() {
        closeDetailsView();
        showSection(Section.SEARCH);
    }

    private void closeDetailsView() {
        if (detailsView != null) {
            detailsView.cancelActiveLoad();
            detailsView = null;
        }
    }

    private enum Section {
        SEARCH("Search"),
        WATCHLIST("Watchlist"),
        WATCHED("Watched");

        private final String label;

        Section(String label) {
            this.label = label;
        }
    }
}
