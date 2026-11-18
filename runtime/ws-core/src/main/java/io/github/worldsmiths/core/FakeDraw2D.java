package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recording {@link Draw2D} for headless tests. Captures projection, color, and draw commands without
 * touching GL.
 */
public final class FakeDraw2D implements Draw2D {
    private float orthoX = Float.NaN;
    private float orthoY = Float.NaN;
    private float orthoWidth = Float.NaN;
    private float orthoHeight = Float.NaN;
    private float colorR = 1f;
    private float colorG = 1f;
    private float colorB = 1f;
    private float colorA = 1f;
    private DrawMode activeMode;
    private boolean disposed;
    private final List<Command> commands = new ArrayList<Command>();

    @Override
    public void setOrtho2D(float x, float y, float width, float height) {
        orthoX = x;
        orthoY = y;
        orthoWidth = width;
        orthoHeight = height;
        commands.add(Command.ortho(x, y, width, height));
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        colorR = r;
        colorG = g;
        colorB = b;
        colorA = a;
        commands.add(Command.color(r, g, b, a));
    }

    @Override
    public void begin(DrawMode mode) {
        activeMode = mode;
        commands.add(Command.begin(mode));
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        commands.add(Command.line(x1, y1, x2, y2, colorR, colorG, colorB, colorA));
    }

    @Override
    public void rect(float x, float y, float width, float height) {
        commands.add(Command.rect(x, y, width, height, colorR, colorG, colorB, colorA));
    }

    @Override
    public void end() {
        activeMode = null;
        commands.add(Command.end());
    }

    @Override
    public void dispose() {
        disposed = true;
        commands.add(Command.dispose());
    }

    public float lastOrthoX() {
        return orthoX;
    }

    public float lastOrthoY() {
        return orthoY;
    }

    public float lastOrthoWidth() {
        return orthoWidth;
    }

    public float lastOrthoHeight() {
        return orthoHeight;
    }

    public boolean disposed() {
        return disposed;
    }

    /** DrawMode of the open batch, or {@code null} when not between begin/end. */
    public DrawMode activeMode() {
        return activeMode;
    }

    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    public void clear() {
        commands.clear();
        activeMode = null;
        orthoX = Float.NaN;
        orthoY = Float.NaN;
        orthoWidth = Float.NaN;
        orthoHeight = Float.NaN;
        colorR = 1f;
        colorG = 1f;
        colorB = 1f;
        colorA = 1f;
        disposed = false;
    }

    /** One recorded Draw2D call. */
    public static final class Command {
        public enum Kind {
            ORTHO,
            COLOR,
            BEGIN,
            LINE,
            RECT,
            END,
            DISPOSE
        }

        public final Kind kind;
        public final DrawMode mode;
        /** LINE: x1; ORTHO/RECT: x. */
        public final float x1;
        /** LINE: y1; ORTHO/RECT: y. */
        public final float y1;
        /** LINE: x2; ORTHO/RECT: width. */
        public final float x2;
        /** LINE: y2; ORTHO/RECT: height. */
        public final float y2;
        public final float r;
        public final float g;
        public final float b;
        public final float alpha;

        private Command(
                Kind kind,
                DrawMode mode,
                float x1,
                float y1,
                float x2,
                float y2,
                float r,
                float g,
                float b,
                float alpha) {
            this.kind = kind;
            this.mode = mode;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.r = r;
            this.g = g;
            this.b = b;
            this.alpha = alpha;
        }

        static Command ortho(float x, float y, float width, float height) {
            return new Command(Kind.ORTHO, null, x, y, width, height, 0f, 0f, 0f, 0f);
        }

        static Command color(float r, float g, float b, float a) {
            return new Command(Kind.COLOR, null, 0f, 0f, 0f, 0f, r, g, b, a);
        }

        static Command begin(DrawMode mode) {
            return new Command(Kind.BEGIN, mode, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
        }

        static Command line(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
            return new Command(Kind.LINE, null, x1, y1, x2, y2, r, g, b, a);
        }

        static Command rect(
                float x, float y, float width, float height, float r, float g, float b, float a) {
            return new Command(Kind.RECT, null, x, y, width, height, r, g, b, a);
        }

        static Command end() {
            return new Command(Kind.END, null, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
        }

        static Command dispose() {
            return new Command(Kind.DISPOSE, null, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
        }
    }
}
