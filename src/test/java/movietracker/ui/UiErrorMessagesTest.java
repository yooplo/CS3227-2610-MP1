package movietracker.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UiErrorMessagesTest {

    @Test
    void trackingFailuresUseStableNonTechnicalMessages() {
        assertEquals("Could not save the tracking change. Check file access and try again.",
                UiErrorMessages.trackingSaveFailure());
        assertEquals("The tracking change failed unexpectedly. Try again.",
                UiErrorMessages.unexpectedTrackingFailure());
    }

    @Test
    void startupStorageFailureExplainsPreservationWithoutTechnicalDetails() {
        assertEquals(
                "Your locally tracked movie data could not be loaded. "
                        + "The existing data file was left unchanged. Check that it is readable "
                        + "and valid, then restart Movie Tracker.",
                UiErrorMessages.storageStartupFailure());
    }
}
