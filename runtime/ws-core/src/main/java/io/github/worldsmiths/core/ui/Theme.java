package io.github.worldsmiths.core.ui;

/** Color and typography roles for a {@link UiKit} session. */
public final class Theme {

  public final float panelR;
  public final float panelG;
  public final float panelB;
  public final float panelA;
  public final float buttonR;
  public final float buttonG;
  public final float buttonB;
  public final float buttonA;
  public final float focusR;
  public final float focusG;
  public final float focusB;
  public final float focusA;

  public static Theme defaults() {
    return new Theme(0.12f, 0.12f, 0.14f, 0.9f, 0.25f, 0.45f, 0.85f, 1f, 1f, 0.85f, 0.2f, 1f);
  }

  public Theme(
      float panelR,
      float panelG,
      float panelB,
      float panelA,
      float buttonR,
      float buttonG,
      float buttonB,
      float buttonA,
      float focusR,
      float focusG,
      float focusB,
      float focusA) {
    this.panelR = panelR;
    this.panelG = panelG;
    this.panelB = panelB;
    this.panelA = panelA;
    this.buttonR = buttonR;
    this.buttonG = buttonG;
    this.buttonB = buttonB;
    this.buttonA = buttonA;
    this.focusR = focusR;
    this.focusG = focusG;
    this.focusB = focusB;
    this.focusA = focusA;
  }
}