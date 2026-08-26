package movietracker.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TmdbImageUrlsTest {

    @Test
    void posterUri_buildsW342HttpsUri() {
        assertEquals("https://image.tmdb.org/t/p/w342/poster.jpg",
                TmdbImageUrls.posterUri("/poster.jpg").orElseThrow().toString());
        assertEquals("https://image.tmdb.org/t/p/w342/poster.jpg",
                TmdbImageUrls.posterUri("poster.jpg").orElseThrow().toString());
    }

    @Test
    void posterUri_missingPath_returnsEmpty() {
        assertTrue(TmdbImageUrls.posterUri(null).isEmpty());
        assertTrue(TmdbImageUrls.posterUri("   ").isEmpty());
    }
}
