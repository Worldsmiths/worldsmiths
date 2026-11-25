package io.github.worldsmiths.core.ui;

import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recording {@link UiKit} for headless tests. Leaf Kind recording and measure seeds land in T2;
 * T1 supplies the bag shell and an empty command log.
 */
public final class FakeUiKit implements UiKit {
    private final List<Command> commands = new ArrayList<Command>();
    private final FakeFocusScope focusScope = new FakeFocusScope();

    @Override
    public void setTheme(Theme theme) {}

    @Override
    public void setViewport(int width, int height, Insets insets, float uiScale) {}

    @Override
    public void clear() {
        commands.clear();
    }

    @Override
    public Button button(String id, String label) {
        return new FakeButton(id);
    }

    @Override
    public Label label(String id, String text) {
        return new FakeLabel(id);
    }

    @Override
    public FocusScope focusScope() {
        return focusScope;
    }

    @Override
    public void layout() {}

    @Override
    public UiHit hit(float x, float y) {
        return UiHit.none();
    }

    @Override
    public void draw(Draw2D draw2d, Font font) {}

    /** Append-only interaction log; {@link #clear()} empties it for the next frame. */
    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    /** Clears the recording log only (not a full session reset). */
    public void clearCommands() {
        commands.clear();
    }

    /** Immutable Fake recording entry. Kinds are populated in T2. */
    public static final class Command {
        public enum Kind {
            CREATE,
            LAYOUT,
            HIT,
            FOCUS,
            ACTIVATE,
            DISPOSE
        }

        public final Kind kind;
        public final String id;

        Command(Kind kind, String id) {
            this.kind = kind;
            this.id = id;
        }
    }

    private static final class FakeButton implements Button {
        private final String id;

        FakeButton(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }

    private static final class FakeLabel implements Label {
        private final String id;

        FakeLabel(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }

    private static final class FakeFocusScope implements FocusScope {
        @Override
        public void add(Button button) {}
    }
}