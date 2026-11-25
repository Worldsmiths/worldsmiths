package io.github.worldsmiths.core.ui;

import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.Font;

/** Pure-Java session kit for production bags. Leaf behaviour lands in T2. */
public final class DefaultUiKit implements UiKit {
    private final DefaultFocusScope focusScope = new DefaultFocusScope();

    @Override
    public void setTheme(Theme theme) {}

    @Override
    public void setViewport(int width, int height, Insets insets, float uiScale) {}

    @Override
    public void clear() {}

    @Override
    public Button button(String id, String label) {
        return new DefaultButton(id);
    }

    @Override
    public Label label(String id, String text) {
        return new DefaultLabel(id);
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

    private static final class DefaultButton implements Button {
        private final String id;

        DefaultButton(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }

    private static final class DefaultLabel implements Label {
        private final String id;

        DefaultLabel(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }

    private static final class DefaultFocusScope implements FocusScope {
        @Override
        public void add(Button button) {}
    }
}