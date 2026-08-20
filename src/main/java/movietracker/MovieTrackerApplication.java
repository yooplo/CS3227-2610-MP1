package movietracker;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import movietracker.application.MovieCollectionManager;
import movietracker.model.MovieCollection;
import movietracker.service.MovieApiService;
import movietracker.service.TmdbMovieApiService;
import movietracker.storage.MovieStorage;
import movietracker.storage.StorageException;
import movietracker.ui.MainController;

public class MovieTrackerApplication extends Application {
    private static final String APPLICATION_TITLE = "Movie Tracker";
    private static final String MAIN_VIEW = "/movietracker/view/MainView.fxml";
    private static final String STORAGE_LOAD_WARNING = "Saved movie data could not be loaded. "
            + "The existing data file was preserved, and changes will only last for this session.";

    @Override
    public void start(Stage stage) throws IOException {
        MovieApiService movieApiService = new TmdbMovieApiService();
        MovieStorage movieStorage = new MovieStorage(java.nio.file.Path.of("data", "movies.json"));
        MovieCollection movieCollection;
        boolean persistenceEnabled = true;
        String startupWarning = "";
        try {
            movieCollection = movieStorage.load();
        } catch (StorageException exception) {
            movieCollection = new MovieCollection();
            persistenceEnabled = false;
            startupWarning = STORAGE_LOAD_WARNING;
        }
        MovieCollectionManager movieCollectionManager = new MovieCollectionManager(
                movieCollection, movieStorage, persistenceEnabled);
        FXMLLoader loader = new FXMLLoader(MovieTrackerApplication.class.getResource(MAIN_VIEW));
        String controllerStartupWarning = startupWarning;
        loader.setControllerFactory(controllerType -> {
            if (controllerType == MainController.class) {
                return new MainController(
                        movieApiService, movieCollectionManager, controllerStartupWarning);
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
