package io.github.worldsmiths.core.ui;

/** Axis-aligned bounds in logical top-left space (Y increases downward). */
public final class UiBounds {
  public final float x;
  public final float y;
  public final float width;
  public final float height;

  public UiBounds(float x, float y, float width, float height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public float centerX() {
    return x + width * 0.5f;
  }

  public float centerY() {
    return y + height * 0.5f;
  }

  public boolean contains(float px, float py) {
    return px >= x && px < x + width && py >= y && py < y + height;
  }

  /** Expands around the center so width and height are at least {@code min}. */
  public UiBounds expandToMinSize(float min) {
    float w = Math.max(width, min);
    float h = Math.max(height, min);
    float dx = (w - width) * 0.5f;
    float dy = (h - height) * 0.5f;
    return new UiBounds(x - dx, y - dy, w, h);
  }

  public boolean overlaps(UiBounds other) {
    return x < other.x + other.width
        && x + width > other.x
        && y < other.y + other.height
        && y + height > other.y;
  }
}