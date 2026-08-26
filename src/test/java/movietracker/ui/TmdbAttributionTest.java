package movietracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class TmdbAttributionTest {
    @Test
    void noticeMatchesTmdbRequiredWording() {
        assertEquals(
                "This product uses the TMDB API but is not endorsed or certified by TMDB.",
                TmdbAttribution.NOTICE);
    }

    @Test
    void approvedLogoResourceIsBundled() throws IOException {
        try (InputStream stream = TmdbAttributionTest.class
                .getResourceAsStream(TmdbAttribution.LOGO_RESOURCE)) {
            assertNotNull(stream);
            String svg = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(svg.contains("viewBox=\"0 0 489.04 35.4\""));
            assertTrue(svg.contains("stop-color=\"#90cea1\""));
            assertTrue(svg.contains("stop-color=\"#00b3e5\""));
        }
    }
}
