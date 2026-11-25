package io.github.worldsmiths.core.ui;

/** Read-only text leaf. */
public interface Label {

  String id();

  String text();

  UiBounds bounds();
}