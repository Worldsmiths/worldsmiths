package io.github.worldsmiths.core.ui;

/** Production measure using fixed per-glyph advances. */
final class DefaultUiMeasure implements UiMeasure {

  static final float CHAR_ADVANCE = 8f;
  static final float LINE_HEIGHT = 16f;

  @Override
  public float advance(String text) {
    if (text == null || text.isEmpty()) {
      return 0f;
    }
    return text.length() * CHAR_ADVANCE;
  }

  @Override
  public float lineHeight() {
    return LINE_HEIGHT;
  }
}