package movietracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import movietracker.api.TmdbErrorCategory;

class TmdbErrorMessagesTest {

    @Test
    void categoriesMapToStableUserFriendlyMessages() {
        assertEquals("TMDB API token is not configured.",
                TmdbErrorMessages.forCategory(TmdbErrorCategory.MISSING_TOKEN));
        assertEquals("Unable to connect to TMDB. Check your connection and try again.",
                TmdbErrorMessages.forCategory(TmdbErrorCategory.NETWORK));
        assertEquals("TMDB took too long to respond. Try again.",
                TmdbErrorMessages.forCategory(TmdbErrorCategory.TIMEOUT));
        assertEquals("TMDB returned an error. Try again later.",
                TmdbErrorMessages.forCategory(TmdbErrorCategory.HTTP_ERROR));
        assertEquals("TMDB returned invalid data. Try again later.",
                TmdbErrorMessages.forCategory(TmdbErrorCategory.INVALID_RESPONSE));
        assertEquals("The TMDB request was interrupted. Try again.",
                TmdbErrorMessages.forCategory(TmdbErrorCategory.INTERRUPTED));
    }
}
