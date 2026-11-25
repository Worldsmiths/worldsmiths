package io.github.worldsmiths.core.ui;

import io.github.worldsmiths.core.Draw2D;
import io.github.worldsmiths.core.DrawMode;

/** Toolkit-internal glyphs over public line/rect only. */
final class UiGlyphs {

  private UiGlyphs() {}

  static void roundedPanel(Draw2D draw2d, float x, float y, float w, float h, float radius) {
    float r = Math.min(radius, Math.min(w, h) * 0.5f);
    draw2d.rect(x + r, y, w - 2f * r, h);
    draw2d.rect(x, y + r, w, h - 2f * r);
    draw2d.line(x + r, y, x + w - r, y);
    draw2d.line(x + r, y + h, x + w - r, y + h);
    draw2d.line(x, y + r, x, y + h - r);
    draw2d.line(x + w, y + r, x + w, y + h - r);
  }

  static void orb(Draw2D draw2d, float cx, float cy, float radius, int strips) {
    if (strips < 4) {
      strips = 4;
    }
    float step = (float) (Math.PI * 2.0 / strips);
    for (int i = 0; i < strips; i++) {
      float a0 = i * step;
      float a1 = (i + 1) * step;
      float x0 = cx + (float) Math.cos(a0) * radius;
      float y0 = cy + (float) Math.sin(a0) * radius;
      float x1 = cx + (float) Math.cos(a1) * radius;
      float y1 = cy + (float) Math.sin(a1) * radius;
      draw2d.line(cx, cy, x0, y0);
      draw2d.line(x0, y0, x1, y1);
    }
  }

  static void slash(Draw2D draw2d, float x, float y, float size) {
    draw2d.line(x, y + size, x + size, y);
    draw2d.line(x + size * 0.2f, y, x + size, y + size * 0.8f);
  }

  static void swatchFill(Draw2D draw2d, float x, float y, float size) {
    draw2d.rect(x, y, size, size);
    float inset = size * 0.15f;
    draw2d.line(x + inset, y + inset, x + size - inset, y + size - inset);
  }

  static void beginFilled(Draw2D draw2d) {
    draw2d.begin(DrawMode.FILLED);
  }

  static void beginLine(Draw2D draw2d) {
    draw2d.begin(DrawMode.LINE);
  }
}