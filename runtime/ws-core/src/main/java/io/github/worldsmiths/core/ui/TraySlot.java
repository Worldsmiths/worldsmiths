package io.github.worldsmiths.core.ui;

/** Ordered dynamic tray row (Puzzle / PvP). */
public interface TraySlot {

  String id();

  /** Number of slots in the row (0 or more). */
  TraySlot length(int count);

  int length();

  Button slot(int index, String slotId, String label);

  UiBounds bounds();
}