package io.github.worldsmiths.core.ui;

/** Modal overlay with dismiss control; captures hit while visible. */
public interface ModalOverlay {

  String id();

  Button dismiss(String dismissId, String label);

  UiBounds bounds();
}