package movietracker.ui;

import java.net.URI;
import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import movietracker.api.TmdbImageUrls;

/**
 * Reusable, non-blocking TMDB poster display with a resilient placeholder.
 */
final class PosterView extends StackPane {

    PosterView(Optional<String> posterPath, double width, double height) {
        Label placeholder = new Label("No poster available");
        placeholder.getStyleClass().add("poster-placeholder");
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setWrapText(true);
        placeholder.setPrefSize(width, height);
        placeholder.setMinSize(width, height);

        getStyleClass().add("poster-area");
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        getChildren().add(placeholder);

        posterPath.flatMap(TmdbImageUrls::posterUri)
                .ifPresent(uri -> loadPoster(uri, placeholder, width, height));
    }

    private void loadPoster(URI uri, Label placeholder, double width, double height) {
        placeholder.setText("Loading poster...");
        Image image = new Image(uri.toString(), width, height, true, true, true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setVisible(false);
        getChildren().add(imageView);

        image.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 1.0 && !image.isError()) {
                imageView.setVisible(true);
                placeholder.setVisible(false);
            }
        });
        image.errorProperty().addListener((observable, oldValue, hasError) -> {
            if (hasError) {
                imageView.setVisible(false);
                placeholder.setVisible(true);
                placeholder.setText("Poster unavailable");
            }
        });
    }
}
