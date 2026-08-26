package movietracker.ui;

/**
 * Stable user-facing messages for local application failures.
 */
public final class UiErrorMessages {

    private UiErrorMessages() {
    }

    public static String trackingSaveFailure() {
        return "Could not save the tracking change. Check file access and try again.";
    }

    public static String unexpectedTrackingFailure() {
        return "The tracking change failed unexpectedly. Try again.";
    }

    /**
     * Explains a startup storage failure without exposing file contents or technical details.
     */
    public static String storageStartupFailure() {
        return "Your locally tracked movie data could not be loaded. "
                + "The existing data file was left unchanged. Check that it is readable "
                + "and valid, then restart Movie Tracker.";
    }
}
