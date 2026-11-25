package io.github.worldsmiths.core.ui;

/** Focusable action leaf. */
public interface Button {

  String id();

  String label();

  Button enabled(boolean enabled);

  boolean enabled();

  UiBounds bounds();
}