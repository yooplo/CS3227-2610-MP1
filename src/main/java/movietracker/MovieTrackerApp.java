package movietracker;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for Movie Tracker.
 */
public final class MovieTrackerApp extends Application {

    private static final String APPLICATION_TITLE = "Movie Tracker";
    private static final String PLACEHOLDER_TEXT = "Movie Tracker is under development.";

    /**
     * Starts the JavaFX application.
     *
     * @param primaryStage the primary application window
     */
    @Override
    public void start(Stage primaryStage) {
        Label placeholder = new Label(PLACEHOLDER_TEXT);
        StackPane root = new StackPane(placeholder);
        root.setAlignment(Pos.CENTER);

        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setScene(new Scene(root, 640, 400));
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(250);
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
