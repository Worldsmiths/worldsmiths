package io.github.worldsmiths.samples.relay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RelayTouchGesturesTest {

    @Test
    void releaseYieldsZeroAxesAndNoRestartWhenNotTerminal() {
        RelayMoveIntent intent =
                RelayTouchGestures.release(100f, 200f, 105f, 204f, false);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertFalse(intent.restart());
    }

    @Test
    void tapWhenTerminalRequestsRestartWithZeroAxes() {
        RelayMoveIntent intent =
                RelayTouchGestures.release(100f, 200f, 108f, 205f, true);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertTrue(intent.restart());
    }

    @Test
    void longDragReleaseWhenTerminalDoesNotRestart() {
        RelayMoveIntent intent =
                RelayTouchGestures.release(100f, 200f, 200f, 200f, true);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertFalse(intent.restart());
    }

    @Test
    void dragRightMapsToPositiveX() {
        RelayMoveIntent intent =
                RelayTouchGestures.dragStick(
                        100f, 200f, 100f + RelayTouchGestures.FULL_DEFLECTION_PX, 200f, false);
        assertEquals(1f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertFalse(intent.restart());
    }

    @Test
    void dragScreenUpMapsToNegativeZ() {
        RelayMoveIntent intent =
                RelayTouchGestures.dragStick(
                        100f, 200f, 100f, 200f - RelayTouchGestures.FULL_DEFLECTION_PX, false);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(-1f, intent.axisZ(), 1e-5f);
    }

    @Test
    void dragInsideDeadZoneIsZero() {
        float half = RelayTouchGestures.DEAD_ZONE_PX * 0.5f;
        RelayMoveIntent intent =
                RelayTouchGestures.dragStick(100f, 200f, 100f + half, 200f + half, false);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
    }

    @Test
    void dragWhileTerminalZerosAxes() {
        RelayMoveIntent intent =
                RelayTouchGestures.dragStick(
                        100f, 200f, 100f + RelayTouchGestures.FULL_DEFLECTION_PX, 200f, true);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertFalse(intent.restart());
    }
}
