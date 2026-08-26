package movietracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UiErrorMessagesTest {

    @Test
    void trackingFailuresUseStableNonTechnicalMessages() {
        assertEquals("Could not save the Watchlist change. Check file access and try again.",
                UiErrorMessages.trackingSaveFailure());
        assertEquals("The Watchlist change failed unexpectedly. Try again.",
                UiErrorMessages.unexpectedTrackingFailure());
    }
}
