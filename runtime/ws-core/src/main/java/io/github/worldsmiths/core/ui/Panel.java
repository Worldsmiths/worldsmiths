package io.github.worldsmiths.core.ui;

/** Container leaf for HUD regions. */
public interface Panel {

  String id();

  Label label(String labelId, String text);

  UiBounds bounds();
}