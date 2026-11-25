package io.github.worldsmiths.core.ui;

/** Named text role within a {@link Theme}. */
public final class TextStyle {

  public final float size;

  public TextStyle(float size) {
    this.size = size;
  }

  public static TextStyle body() {
    return new TextStyle(14f);
  }
}