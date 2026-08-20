package movietracker;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import movietracker.service.MovieApiService;
import movietracker.service.TmdbMovieApiService;
import movietracker.ui.MainController;

public class MovieTrackerApplication extends Application {
    private static final String APPLICATION_TITLE = "Movie Tracker";
    private static final String MAIN_VIEW = "/movietracker/view/MainView.fxml";

    @Override
    public void start(Stage stage) throws IOException {
        MovieApiService movieApiService = new TmdbMovieApiService();
        FXMLLoader loader = new FXMLLoader(MovieTrackerApplication.class.getResource(MAIN_VIEW));
        loader.setControllerFactory(controllerType -> {
            if (controllerType == MainController.class) {
                return new MainController(movieApiService);
            }
            throw new IllegalArgumentException("Unsupported controller: " + controllerType.getName());
        });

        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle(APPLICATION_TITLE);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
}
