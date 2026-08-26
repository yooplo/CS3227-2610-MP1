package movietracker.ui;

/**
 * Stable user-facing messages for local application failures.
 */
final class UiErrorMessages {

    private UiErrorMessages() {
    }

    static String trackingSaveFailure() {
        return "Could not save the tracking change. Check file access and try again.";
    }

    static String unexpectedTrackingFailure() {
        return "The tracking change failed unexpectedly. Try again.";
    }
}
