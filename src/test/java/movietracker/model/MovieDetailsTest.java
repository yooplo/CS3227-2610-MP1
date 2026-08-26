package movietracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class MovieDetailsTest {

    private static final Movie MOVIE = new Movie(
            550, "Fight Club", LocalDate.of(1999, 10, 15), "/poster.jpg");

    @Test
    void constructor_validValues_retainsAndNormalizesDetails() {
        MovieDetails details = new MovieDetails(
                MOVIE,
                "  An insomniac meets a soap maker.  ",
                139,
                List.of(" Drama ", "Thriller"),
                " /backdrop.jpg ",
                8.4);

        assertEquals(MOVIE, details.getMovie());
        assertEquals("An insomniac meets a soap maker.", details.getOverview().orElseThrow());
        assertEquals(139, details.getRuntimeMinutes().orElseThrow());
        assertEquals(List.of("Drama", "Thriller"), details.getGenres());
        assertEquals("/backdrop.jpg", details.getBackdropPath().orElseThrow());
        assertEquals(8.4, details.getTmdbVoteAverage().orElseThrow());
    }

    @Test
    void constructor_missingOptionalValues_exposesEmptyValues() {
        MovieDetails details = new MovieDetails(MOVIE, "  ", null, List.of(), null, null);

        assertTrue(details.getOverview().isEmpty());
        assertTrue(details.getRuntimeMinutes().isEmpty());
        assertTrue(details.getGenres().isEmpty());
        assertTrue(details.getBackdropPath().isEmpty());
        assertTrue(details.getTmdbVoteAverage().isEmpty());
    }

    @Test
    void constructor_runtimeAndVoteBoundaries_acceptsValidBoundaries() {
        MovieDetails minimum = new MovieDetails(MOVIE, null, 0, List.of(), null, 0.0);
        MovieDetails maximum = new MovieDetails(MOVIE, null, 1, List.of(), null, 10.0);

        assertEquals(0, minimum.getRuntimeMinutes().orElseThrow());
        assertEquals(0.0, minimum.getTmdbVoteAverage().orElseThrow());
        assertEquals(10.0, maximum.getTmdbVoteAverage().orElseThrow());
    }

    @Test
    void constructor_invalidValues_throwsException() {
        assertThrows(NullPointerException.class,
                () -> new MovieDetails(null, null, null, List.of(), null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new MovieDetails(MOVIE, null, -1, List.of(), null, null));
        assertThrows(NullPointerException.class,
                () -> new MovieDetails(MOVIE, null, null, null, null, null));
        assertThrows(NullPointerException.class,
                () -> new MovieDetails(MOVIE, null, null,
                        Arrays.asList("Drama", null), null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new MovieDetails(MOVIE, null, null, List.of("  "), null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new MovieDetails(MOVIE, null, null, List.of(), null, -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> new MovieDetails(MOVIE, null, null, List.of(), null, 10.1));
        assertThrows(IllegalArgumentException.class,
                () -> new MovieDetails(MOVIE, null, null, List.of(), null, Double.NaN));
    }

    @Test
    void genres_areDefensivelyCopiedAndUnmodifiable() {
        List<String> sourceGenres = new ArrayList<>(List.of("Drama"));
        MovieDetails details = new MovieDetails(MOVIE, null, null, sourceGenres, null, null);

        sourceGenres.add("Thriller");

        assertEquals(List.of("Drama"), details.getGenres());
        assertThrows(UnsupportedOperationException.class,
                () -> details.getGenres().add("Crime"));
    }

    @Test
    void equality_sameTmdbId_equalDespiteDifferentDetails() {
        Movie refreshedMovie = new Movie(550, "Updated title", null, null);
        MovieDetails first = new MovieDetails(MOVIE, "First", 139, List.of("Drama"), null, 8.4);
        MovieDetails refreshed = new MovieDetails(
                refreshedMovie, "Updated", null, List.of(), "/new.jpg", null);
        MovieDetails different = new MovieDetails(
                new Movie(551, "Different", null, null), null, null, List.of(), null, null);

        assertEquals(first, refreshed);
        assertEquals(first.hashCode(), refreshed.hashCode());
        assertNotEquals(first, different);
    }
}
