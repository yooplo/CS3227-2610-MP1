package movietracker;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import movietracker.ui.MainWindow;

/**
 * JavaFX application entry point for Movie Tracker.
 */
public final class MovieTrackerApp extends Application {

    private static final String APPLICATION_TITLE = "Movie Tracker";
    private static final double INITIAL_WIDTH = 960;
    private static final double INITIAL_HEIGHT = 640;
    private static final double MINIMUM_WIDTH = 720;
    private static final double MINIMUM_HEIGHT = 480;

    /**
     * Starts the JavaFX application.
     *
     * @param primaryStage the primary application window
     */
    @Override
    public void start(Stage primaryStage) {
        MainWindow root = new MainWindow();
        Scene scene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);
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

    /**
     * Launches the JavaFX runtime.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }
}
