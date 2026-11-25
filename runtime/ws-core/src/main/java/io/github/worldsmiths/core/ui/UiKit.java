package io.github.worldsmiths.core.ui;

import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.Font;

/**
 * Session widget toolkit: hybrid leaves, layout, hit, focus, and draw via {@link Draw2D} / {@link
 * Font}. Obtain via {@link io.github.worldsmiths.core.EngineServices#uiKit()}.
 */
public interface UiKit {

  void setTheme(Theme theme);

  void setViewport(int width, int height, Insets insets, float uiScale);

  /** Drop all roots; idempotent. */
  void clear();

  Button button(String id, String label);

  Label label(String id, String text);

  Panel panel(String id);

  TraySlot traySlot(String id);

  Swatch swatch(String id);

  ModalOverlay modalOverlay(String id);

  FocusScope focusScope();

  void layout();

  UiHit hit(float x, float y);

  void draw(Draw2D draw2d, Font font);
}