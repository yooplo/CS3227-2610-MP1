package movietracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import movietracker.api.TmdbErrorCategory;

class SearchErrorMessagesTest {

    @Test
    void categoriesMapToStableUserFriendlyMessages() {
        assertEquals("TMDB API token is not configured.",
                SearchErrorMessages.forCategory(TmdbErrorCategory.MISSING_TOKEN));
        assertEquals("Unable to connect to TMDB. Check your connection and try again.",
                SearchErrorMessages.forCategory(TmdbErrorCategory.NETWORK));
        assertEquals("TMDB took too long to respond. Try again.",
                SearchErrorMessages.forCategory(TmdbErrorCategory.TIMEOUT));
        assertEquals("TMDB returned an error. Try again later.",
                SearchErrorMessages.forCategory(TmdbErrorCategory.HTTP_ERROR));
        assertEquals("TMDB returned invalid data. Try again later.",
                SearchErrorMessages.forCategory(TmdbErrorCategory.INVALID_RESPONSE));
        assertEquals("The TMDB request was interrupted. Try again.",
                SearchErrorMessages.forCategory(TmdbErrorCategory.INTERRUPTED));
    }
}
