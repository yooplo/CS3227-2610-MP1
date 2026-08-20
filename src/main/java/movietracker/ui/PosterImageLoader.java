package movietracker.ui;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

final class PosterImageLoader {
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w342";
    private static final int MAX_CACHED_IMAGES = 100;

    private final Map<String, Image> imageCache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
            return size() > MAX_CACHED_IMAGES;
        }
    };

    StackPane createPoster(String posterPath, double width, double height) {
        Label placeholderText = new Label("POSTER");
        placeholderText.getStyleClass().add("poster-placeholder-text");

        StackPane container = new StackPane(placeholderText);
        container.setMinSize(width, height);
        container.setPrefSize(width, height);
        container.setMaxSize(width, height);
        container.getStyleClass().add("poster-placeholder");

        Optional<String> imageUrl = buildPosterUrl(posterPath);
        if (imageUrl.isEmpty()) {
            return container;
        }

        Image image;
        try {
            image = imageCache.computeIfAbsent(
                    imageUrl.orElseThrow(),
                    url -> new Image(url, true));
        } catch (RuntimeException exception) {
            return container;
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setVisible(false);
        imageView.setMouseTransparent(true);
        imageView.setAccessibleText("Movie poster");
        imageView.getStyleClass().add("poster-image");
        container.getChildren().add(imageView);

        new ImageLoadObserver(image, imageView, placeholderText).start();
        return container;
    }

    static Optional<String> buildPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return Optional.empty();
        }

        String normalizedPath = posterPath.strip();
        if (normalizedPath.contains("..")
                || normalizedPath.contains("\\")
                || normalizedPath.contains("?")
                || normalizedPath.contains("#")
                || normalizedPath.contains("://")) {
            return Optional.empty();
        }
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }

        String url = IMAGE_BASE_URL + normalizedPath;
        try {
            URI.create(url);
            return Optional.of(url);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static final class ImageLoadObserver {
        private final Image image;
        private final ImageView imageView;
        private final Label placeholderText;
        private final ChangeListener<Number> progressListener = this::onProgressChanged;
        private final ChangeListener<Boolean> errorListener = this::onErrorChanged;
        private boolean finished;

        private ImageLoadObserver(Image image, ImageView imageView, Label placeholderText) {
            this.image = image;
            this.imageView = imageView;
            this.placeholderText = placeholderText;
        }

        private void start() {
            image.progressProperty().addListener(progressListener);
            image.errorProperty().addListener(errorListener);
            if (image.isError() || image.getProgress() >= 1.0) {
                finish();
            }
        }

        private void onProgressChanged(
                javafx.beans.value.ObservableValue<? extends Number> observable,
                Number oldValue,
                Number newValue) {
            if (newValue.doubleValue() >= 1.0) {
                finish();
            }
        }

        private void onErrorChanged(
                javafx.beans.value.ObservableValue<? extends Boolean> observable,
                Boolean oldValue,
                Boolean newValue) {
            if (Boolean.TRUE.equals(newValue)) {
                finish();
            }
        }

        private void finish() {
            if (finished) {
                return;
            }
            finished = true;
            image.progressProperty().removeListener(progressListener);
            image.errorProperty().removeListener(errorListener);

            boolean loaded = !image.isError() && image.getWidth() > 0 && image.getHeight() > 0;
            imageView.setVisible(loaded);
            placeholderText.setVisible(!loaded);
        }
    }
}
