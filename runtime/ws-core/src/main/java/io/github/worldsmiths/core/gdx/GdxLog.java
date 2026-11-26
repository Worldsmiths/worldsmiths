package io.github.worldsmiths.core.gdx;

import com.badlogic.gdx.Gdx;
import io.github.worldsmiths.core.Log;

/**
 * GDX-backed {@link Log} that maps author levels onto Application {@code debug}/{@code log}/{@code
 * error}. Untagged calls use sink tag {@code "WS"}; {@code warn} shares the info channel with a
 * {@code "WARN: "} message prefix. Not part of the author-facing API — backends assemble this via
 * {@link GdxEngineServices}.
 */
final class GdxLog implements Log {
    private static final String DEFAULT_TAG = "WS";

    private final Sink sink;

    GdxLog() {
        this(
                new Sink() {
                    @Override
                    public void debug(String tag, String message) {
                        Gdx.app.debug(tag, message);
                    }

                    @Override
                    public void log(String tag, String message) {
                        Gdx.app.log(tag, message);
                    }

                    @Override
                    public void error(String tag, String message) {
                        Gdx.app.error(tag, message);
                    }

                    @Override
                    public void error(String tag, String message, Throwable exception) {
                        Gdx.app.error(tag, message, exception);
                    }
                });
    }

    /** Package-visible for tests that inject a sink without a live Application. */
    GdxLog(Sink sink) {
        this.sink = sink;
    }

    @Override
    public void debug(String message) {
        debug(DEFAULT_TAG, message);
    }

    @Override
    public void debug(String tag, String message) {
        sink.debug(tag, normalize(message));
    }

    @Override
    public void info(String message) {
        info(DEFAULT_TAG, message);
    }

    @Override
    public void info(String tag, String message) {
        sink.log(tag, normalize(message));
    }

    @Override
    public void warn(String message) {
        warn(DEFAULT_TAG, message);
    }

    @Override
    public void warn(String tag, String message) {
        sink.log(tag, "WARN: " + normalize(message));
    }

    @Override
    public void error(String message) {
        error(DEFAULT_TAG, message);
    }

    @Override
    public void error(String tag, String message) {
        sink.error(tag, normalize(message));
    }

    @Override
    public void error(String message, Throwable cause) {
        error(DEFAULT_TAG, message, cause);
    }

    @Override
    public void error(String tag, String message, Throwable cause) {
        sink.error(tag, normalize(message), cause);
    }

    private static String normalize(String message) {
        return message == null ? "" : message;
    }

    /**
     * Injectable mirror of Application logging methods used by unit tests without a live
     * Application.
     */
    interface Sink {
        void debug(String tag, String message);

        void log(String tag, String message);

        void error(String tag, String message);

        void error(String tag, String message, Throwable exception);
    }
}
