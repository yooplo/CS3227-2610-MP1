package movietracker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import movietracker.api.TmdbClient;
import movietracker.service.MovieTrackerApplicationService;
import movietracker.service.MovieTrackerService;
import movietracker.storage.LocalStorage;
import movietracker.storage.Storage;
import movietracker.storage.StorageException;
import movietracker.ui.MainWindow;
import movietracker.ui.UiErrorMessages;

/**
 * JavaFX application entry point for Movie Tracker.
 */
public final class MovieTrackerApp extends Application {

    private static final String APPLICATION_TITLE = "Movie Tracker";
    private static final double INITIAL_WIDTH = 960;
    private static final double INITIAL_HEIGHT = 640;
    private static final double MINIMUM_WIDTH = 720;
    private static final double MINIMUM_HEIGHT = 480;

    private ExecutorService applicationExecutor;
    private MainWindow mainWindow;

    /**
     * Starts the JavaFX application.
     *
     * @param primaryStage the primary application window
     */
    @Override
    public void start(Stage primaryStage) {
        MovieTrackerApplicationService applicationService;
        try {
            applicationService = createApplicationService(
                    new LocalStorage(), TmdbClient.fromEnvironment());
        } catch (StorageException exception) {
            showStorageStartupFailure(primaryStage);
            return;
        }

        applicationExecutor = Executors.newSingleThreadExecutor(runnable -> Thread.ofPlatform()
                .daemon(true)
                .name("application-worker")
                .unstarted(runnable));

        mainWindow = new MainWindow(applicationService, applicationExecutor);
        Scene scene = new Scene(mainWindow, INITIAL_WIDTH, INITIAL_HEIGHT);
        scene.getStylesheets().add(getClass()
                .getResource("/movietracker/css/app.css")
                .toExternalForm());

        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MINIMUM_WIDTH);
        primaryStage.setMinHeight(MINIMUM_HEIGHT);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    static MovieTrackerApplicationService createApplicationService(
            Storage storage, TmdbClient tmdbClient) throws StorageException {
        MovieTrackerService trackingService = new MovieTrackerService(storage);
        return new MovieTrackerApplicationService(tmdbClient, trackingService);
    }

    private static void showStorageStartupFailure(Stage primaryStage) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(primaryStage);
        alert.setTitle(APPLICATION_TITLE + " — Startup Error");
        alert.setHeaderText("Movie Tracker could not start safely.");
        alert.setContentText(UiErrorMessages.storageStartupFailure());
        alert.showAndWait();
        Platform.exit();
    }

    @Override
    public void stop() {
        if (mainWindow != null) {
            mainWindow.close();
        }
        if (applicationExecutor != null) {
            applicationExecutor.shutdownNow();
        }
    }

    /**
     * Launches the JavaFX runtime.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }
}
