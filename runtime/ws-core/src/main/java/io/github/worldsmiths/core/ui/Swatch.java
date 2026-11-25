package io.github.worldsmiths.core.ui;

/** Theme-tintable color sample leaf. */
public interface Swatch {

  String id();

  Swatch color(float r, float g, float b, float a);

  UiBounds bounds();
}