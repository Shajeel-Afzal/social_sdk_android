package com.layer.xdk.ui.util;

/**
 * Unified Log class used by XDK UI classes that maintains similar signatures to `android.util.Log`.
 * Logs are tagged with `LayerXDK-UI`.
 */
public class Log {
    public static final String TAG = "LayerXDK-UI";
    private static final String PERF_TAG = "LayerPerf:XDK-UI";

    // Makes IDE auto-completion easy
    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static final int DEBUG = android.util.Log.DEBUG;
    public static final int INFO = android.util.Log.INFO;
    public static final int WARN = android.util.Log.WARN;
    public static final int ERROR = android.util.Log.ERROR;

    private static volatile boolean sAlwaysLoggable = false;

    /**
     * Returns `true` if the provided log level is loggable either through environment options or
     * a previous call to setAlwaysLoggable().
     *
     * @param level Log level to check.
     * @return `true` if the provided log level is loggable.
     * @see #setLoggingEnabled(boolean)
     */
    public static boolean isLoggable(int level) {
        return sAlwaysLoggable || android.util.Log.isLoggable(TAG, level);
    }

    public static void setLoggingEnabled(boolean enabled) {
        sAlwaysLoggable = enabled;
    }

    public static void v(String message) {
        android.util.Log.v(TAG, message);
    }

    public static void v(String message, Throwable error) {
        android.util.Log.v(TAG, message, error);
    }

    public static void d(String message) {
        android.util.Log.d(TAG, message);
    }

    public static void d(String message, Throwable error) {
        android.util.Log.d(TAG, message, error);
    }

    public static void i(String message) {
        android.util.Log.i(TAG, message);
    }

    public static void i(String message, Throwable error) {
        android.util.Log.i(TAG, message, error);
    }

    public static void w(String message) {
        android.util.Log.w(TAG, message);
    }

    @SuppressWarnings("unused")
    public static void w(String message, Throwable error) {
        android.util.Log.w(TAG, message, error);
    }

    @SuppressWarnings("unused")
    public static void w(Throwable error) {
        android.util.Log.w(TAG, error);
    }

    public static void e(String message) {
        android.util.Log.e(TAG, message);
    }

    public static void e(String message, Throwable error) {
        android.util.Log.e(TAG, message, error);
    }

    public static boolean isPerfLoggable() {
        return sAlwaysLoggable || android.util.Log.isLoggable(PERF_TAG, DEBUG);
    }

    public static void perf(String message) {
        android.util.Log.d(PERF_TAG, message);
    }
}
