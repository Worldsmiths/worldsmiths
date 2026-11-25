package io.github.worldsmiths.core.ui;

import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.Font;

/** Pure-Java session kit for production bags. */
public final class DefaultUiKit implements UiKit {

  private final UiSession session = new UiSession(new DefaultUiMeasure(), UiRecorder.NONE);

  @Override
  public void setTheme(Theme theme) {
    session.setTheme(theme);
  }

  @Override
  public void setViewport(int width, int height, Insets insets, float uiScale) {
    session.setViewport(width, height, insets, uiScale);
  }

  @Override
  public void clear() {
    session.clear();
  }

  @Override
  public Button button(String id, String label) {
    return session.button(id, label);
  }

  @Override
  public Label label(String id, String text) {
    return session.label(id, text);
  }

  @Override
  public Panel panel(String id) {
    return session.panel(id);
  }

  @Override
  public TraySlot traySlot(String id) {
    return session.traySlot(id);
  }

  @Override
  public Swatch swatch(String id) {
    return session.swatch(id);
  }

  @Override
  public ModalOverlay modalOverlay(String id) {
    return session.modalOverlay(id);
  }

  @Override
  public FocusScope focusScope() {
    return session.focusScope();
  }

  @Override
  public void layout() {
    session.layout();
  }

  @Override
  public UiHit hit(float x, float y) {
    return session.hit(x, y);
  }

  @Override
  public void draw(Draw2D draw2d, Font font) {
    session.draw(draw2d, font);
  }
}