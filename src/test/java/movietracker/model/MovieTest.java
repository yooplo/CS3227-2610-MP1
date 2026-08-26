package movietracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class MovieTest {

    @Test
    void constructor_validValues_normalizesTextAndRetainsMetadata() {
        LocalDate releaseDate = LocalDate.of(1999, 10, 15);
        Movie movie = new Movie(550, "  Fight Club  ", releaseDate, " /poster.jpg ");

        assertEquals(550, movie.getTmdbId());
        assertEquals("Fight Club", movie.getTitle());
        assertEquals(releaseDate, movie.getReleaseDate().orElseThrow());
        assertEquals("/poster.jpg", movie.getPosterPath().orElseThrow());
    }

    @Test
    void constructor_missingOptionalMetadata_exposesEmptyOptionals() {
        Movie movie = new Movie(550, "Fight Club", null, "   ");

        assertTrue(movie.getReleaseDate().isEmpty());
        assertTrue(movie.getPosterPath().isEmpty());
    }

    @Test
    void constructor_invalidIdentityOrTitle_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new Movie(0, "Fight Club", null, null));
        assertThrows(NullPointerException.class,
                () -> new Movie(550, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Movie(550, "  ", null, null));
    }

    @Test
    void equality_sameTmdbId_equalDespiteDifferentSnapshotMetadata() {
        Movie first = new Movie(550, "Fight Club", LocalDate.of(1999, 10, 15), "/first.jpg");
        Movie refreshed = new Movie(550, "Fight Club (Updated)", null, "/second.jpg");
        Movie different = new Movie(551, "Fight Club", null, null);

        assertEquals(first, refreshed);
        assertEquals(first.hashCode(), refreshed.hashCode());
        assertNotEquals(first, different);
        assertFalse(first.equals(null));
    }
}
