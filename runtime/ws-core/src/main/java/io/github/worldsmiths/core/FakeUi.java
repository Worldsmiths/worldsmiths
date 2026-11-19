package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recording {@link Ui} for headless tests. Seed available paths with {@link #putFont}, then assert
 * command kinds ({@code CREATE_FONT}, {@code DRAW_STRING}, {@code DISPOSE}) without a display.
 */
public final class FakeUi implements Ui {
    private final Set<String> available = new HashSet<String>();
    private final List<Command> commands = new ArrayList<Command>();

    /** Marks an internal path as loadable for later {@link #createFont} calls. */
    public void putFont(String internalPath) {
        available.add(internalPath);
    }

    @Override
    public Font createFont(String internalPath) {
        if (!available.contains(internalPath)) {
            throw new EngineException("Missing or unloadable font: " + internalPath);
        }
        commands.add(Command.createFont(internalPath));
        return new FakeFont(internalPath);
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    public void clear() {
        commands.clear();
    }

    private final class FakeFont implements Font {
        private final String path;
        private boolean disposed;

        FakeFont(String path) {
            this.path = path;
        }

        @Override
        public void drawString(String text, float x, float y) {
            commands.add(Command.drawString(path, text, x, y));
        }

        @Override
        public void dispose() {
            if (disposed) {
                return;
            }
            disposed = true;
            commands.add(Command.dispose(path));
        }
    }

    /** One recorded Ui / Font call. */
    public static final class Command {
        public enum Kind {
            CREATE_FONT,
            DRAW_STRING,
            DISPOSE
        }

        public final Kind kind;
        /** Internal path for create / draw / dispose. */
        public final String path;
        /** Drawn text for {@link Kind#DRAW_STRING}; null otherwise. */
        public final String text;
        public final float x;
        public final float y;

        private Command(Kind kind, String path, String text, float x, float y) {
            this.kind = kind;
            this.path = path;
            this.text = text;
            this.x = x;
            this.y = y;
        }

        static Command createFont(String path) {
            return new Command(Kind.CREATE_FONT, path, null, 0f, 0f);
        }

        static Command drawString(String path, String text, float x, float y) {
            return new Command(Kind.DRAW_STRING, path, text, x, y);
        }

        static Command dispose(String path) {
            return new Command(Kind.DISPOSE, path, null, 0f, 0f);
        }
    }
}
