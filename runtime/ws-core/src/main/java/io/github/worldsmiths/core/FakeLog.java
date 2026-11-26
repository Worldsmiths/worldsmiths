package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recording {@link Log} for headless tests. Assert ordered commands (level, message, optional
 * author tag, optional cause) without a platform console.
 */
public final class FakeLog implements Log {
    private final List<Command> commands = new ArrayList<Command>();

    @Override
    public void debug(String message) {
        record(Command.Level.DEBUG, null, message, null);
    }

    @Override
    public void debug(String tag, String message) {
        record(Command.Level.DEBUG, tag, message, null);
    }

    @Override
    public void info(String message) {
        record(Command.Level.INFO, null, message, null);
    }

    @Override
    public void info(String tag, String message) {
        record(Command.Level.INFO, tag, message, null);
    }

    @Override
    public void warn(String message) {
        record(Command.Level.WARN, null, message, null);
    }

    @Override
    public void warn(String tag, String message) {
        record(Command.Level.WARN, tag, message, null);
    }

    @Override
    public void error(String message) {
        record(Command.Level.ERROR, null, message, null);
    }

    @Override
    public void error(String tag, String message) {
        record(Command.Level.ERROR, tag, message, null);
    }

    @Override
    public void error(String message, Throwable cause) {
        record(Command.Level.ERROR, null, message, cause);
    }

    @Override
    public void error(String tag, String message, Throwable cause) {
        record(Command.Level.ERROR, tag, message, cause);
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    public void clear() {
        commands.clear();
    }

    private void record(Command.Level level, String tag, String message, Throwable cause) {
        String recorded = message == null ? "" : message;
        commands.add(new Command(level, recorded, tag, cause));
    }

    /** One recorded Log call. */
    public static final class Command {
        public enum Level {
            DEBUG,
            INFO,
            WARN,
            ERROR
        }

        public final Level level;
        public final String message;
        /** Author tag; {@code null} when the untagged overload was used. */
        public final String tag;
        /** Optional cause; non-null only for error overloads that accept a Throwable. */
        public final Throwable cause;

        private Command(Level level, String message, String tag, Throwable cause) {
            this.level = level;
            this.message = message;
            this.tag = tag;
            this.cause = cause;
        }
    }
}
