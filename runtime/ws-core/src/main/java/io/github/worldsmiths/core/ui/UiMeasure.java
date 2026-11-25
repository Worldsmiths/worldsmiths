package io.github.worldsmiths.core.ui;

/** Toolkit-internal text advance (not public {@link io.github.worldsmiths.core.Font#measure}). */
interface UiMeasure {

  float advance(String text);

  float lineHeight();
}