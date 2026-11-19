package io.github.worldsmiths.samples.relay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RelayMoveIntentTest {

    @Test
    void wasdAndArrowsMapToNormalizedAxes() {
        RelayMoveIntent right =
                RelayMoveIntent.fromKeys(false, true, false, false, false, false);
        assertEquals(1f, right.axisX(), 1e-5f);
        assertEquals(0f, right.axisZ(), 1e-5f);
        assertFalse(right.restart());

        RelayMoveIntent forward =
                RelayMoveIntent.fromKeys(false, false, true, false, false, false);
        assertEquals(0f, forward.axisX(), 1e-5f);
        assertEquals(-1f, forward.axisZ(), 1e-5f);

        RelayMoveIntent diagonal =
                RelayMoveIntent.fromKeys(false, true, true, false, false, false);
        float len =
                (float)
                        Math.sqrt(
                                diagonal.axisX() * diagonal.axisX()
                                        + diagonal.axisZ() * diagonal.axisZ());
        assertEquals(1f, len, 1e-5f);
        assertTrue(diagonal.axisX() > 0f);
        assertTrue(diagonal.axisZ() < 0f);
    }

    @Test
    void terminalIgnoresMoveButAcceptsRestart() {
        RelayMoveIntent intent =
                RelayMoveIntent.fromKeys(true, false, true, false, true, true);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertTrue(intent.restart());
    }

    @Test
    void nonTerminalIgnoresRestartKey() {
        RelayMoveIntent intent =
                RelayMoveIntent.fromKeys(false, false, false, false, true, false);
        assertFalse(intent.restart());
    }

    @Test
    void composeSumsAxesAndHonorsEitherRestart() {
        RelayMoveIntent keys = RelayMoveIntent.of(1f, 0f, false);
        RelayMoveIntent touch = RelayMoveIntent.of(0f, -1f, false);
        RelayMoveIntent merged = RelayMoveIntent.compose(keys, touch);
        float len =
                (float)
                        Math.sqrt(
                                merged.axisX() * merged.axisX()
                                        + merged.axisZ() * merged.axisZ());
        assertEquals(1f, len, 1e-5f);
        assertTrue(merged.axisX() > 0f);
        assertTrue(merged.axisZ() < 0f);
        assertFalse(merged.restart());

        RelayMoveIntent withRestart =
                RelayMoveIntent.compose(keys, RelayMoveIntent.of(0f, 0f, true));
        assertEquals(0f, withRestart.axisX(), 1e-5f);
        assertEquals(0f, withRestart.axisZ(), 1e-5f);
        assertTrue(withRestart.restart());
    }
}
