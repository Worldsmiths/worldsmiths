package io.github.worldsmiths.core.ui;

/** Tab ring for focusable leaves. */
public interface FocusScope {

  void add(Button button);

  void focusNext();

  void focusPrev();

  /** Enter / Space activate on the focused enabled button. */
  void activateFocused();

  String focusedId();
}