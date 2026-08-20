package movietracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PosterImageLoaderTest {
    @Test
    void buildsW342TmdbImageUrlFromPosterPath() {
        assertEquals(
                "https://image.tmdb.org/t/p/w342/abc123.jpg",
                PosterImageLoader.buildPosterUrl("/abc123.jpg").orElseThrow());
        assertEquals(
                "https://image.tmdb.org/t/p/w342/abc123.jpg",
                PosterImageLoader.buildPosterUrl("abc123.jpg").orElseThrow());
    }

    @Test
    void missingPosterPathProducesNoUrl() {
        assertTrue(PosterImageLoader.buildPosterUrl(null).isEmpty());
        assertTrue(PosterImageLoader.buildPosterUrl("").isEmpty());
        assertTrue(PosterImageLoader.buildPosterUrl("   ").isEmpty());
    }

    @Test
    void unsafeOrMalformedPosterPathProducesNoUrl() {
        assertTrue(PosterImageLoader.buildPosterUrl("../poster.jpg").isEmpty());
        assertTrue(PosterImageLoader.buildPosterUrl("/poster.jpg?size=original").isEmpty());
        assertTrue(PosterImageLoader.buildPosterUrl("https://example.com/poster.jpg").isEmpty());
        assertTrue(PosterImageLoader.buildPosterUrl("/poster image.jpg").isEmpty());
    }
}
