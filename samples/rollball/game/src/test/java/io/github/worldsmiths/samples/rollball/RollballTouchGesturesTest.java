package io.github.worldsmiths.samples.rollball;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RollballTouchGesturesTest {

    @Test
    void releaseYieldsZeroAxesAndNoRestartWhenNotTerminal() {
        RollballMoveIntent intent =
                RollballTouchGestures.release(100f, 200f, 105f, 204f, false);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertFalse(intent.restart());
    }

    @Test
    void tapWhenTerminalRequestsRestartWithZeroAxes() {
        RollballMoveIntent intent =
                RollballTouchGestures.release(100f, 200f, 108f, 205f, true);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertTrue(intent.restart());
    }

    @Test
    void longDragReleaseWhenTerminalDoesNotRestart() {
        RollballMoveIntent intent =
                RollballTouchGestures.release(100f, 200f, 200f, 200f, true);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertFalse(intent.restart());
    }

    @Test
    void dragRightMapsToPositiveX() {
        RollballMoveIntent intent =
                RollballTouchGestures.dragStick(
                        100f, 200f, 100f + RollballTouchGestures.FULL_DEFLECTION_PX, 200f, false);
        assertEquals(1f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertFalse(intent.restart());
    }

    @Test
    void dragScreenUpMapsToNegativeZ() {
        // Finger moves up → smaller screen Y → forward (−Z).
        RollballMoveIntent intent =
                RollballTouchGestures.dragStick(
                        100f, 200f, 100f, 200f - RollballTouchGestures.FULL_DEFLECTION_PX, false);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(-1f, intent.axisZ(), 1e-5f);
    }

    @Test
    void dragInsideDeadZoneIsZero() {
        float half = RollballTouchGestures.DEAD_ZONE_PX * 0.5f;
        RollballMoveIntent intent =
                RollballTouchGestures.dragStick(100f, 200f, 100f + half, 200f + half, false);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
    }

    @Test
    void dragWhileTerminalZerosAxes() {
        RollballMoveIntent intent =
                RollballTouchGestures.dragStick(
                        100f, 200f, 100f + RollballTouchGestures.FULL_DEFLECTION_PX, 200f, true);
        assertEquals(0f, intent.axisX(), 1e-5f);
        assertEquals(0f, intent.axisZ(), 1e-5f);
        assertFalse(intent.restart());
    }
}
