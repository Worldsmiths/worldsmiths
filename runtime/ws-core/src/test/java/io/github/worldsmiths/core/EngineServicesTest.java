package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EngineServicesTest {

    @Test
    void fakeBagExposesNonNullFloorServicesAndExtensions() {
        FakeEngineServices services = new FakeEngineServices();

        assertNotNull(services.input());
        assertNotNull(services.clock());
        assertNotNull(services.graphics());
        assertNotNull(services.extensions());
        assertSame(services.input(), services.input());
        assertSame(services.clock(), services.clock());
        assertSame(services.graphics(), services.graphics());
    }

    @Test
    void fakeInputReportsKeyAndPointerStateAuthorsSet() {
        FakeEngineServices services = new FakeEngineServices();
        FakeInput input = services.input();

        assertFalse(input.isKeyDown(Key.LEFT));
        input.setKeyDown(Key.LEFT, true);
        assertTrue(input.isKeyDown(Key.LEFT));

        assertFalse(input.isTouched());
        assertFalse(input.justTouched());
        input.setTouched(true);
        input.setJustTouched(true);
        input.setPointer(12, 34);
        assertTrue(input.isTouched());
        assertTrue(input.justTouched());
        assertEquals(12, input.getX());
        assertEquals(34, input.getY());
    }

    @Test
    void fakeClockReportsDeltaAndTotalAuthorsSet() {
        FakeEngineServices services = new FakeEngineServices();
        FakeClock clock = services.clock();

        assertEquals(0f, clock.delta());
        assertEquals(0f, clock.total());
        clock.setDelta(1f / 60f);
        clock.setTotal(1.5f);
        assertEquals(1f / 60f, clock.delta());
        assertEquals(1.5f, clock.total());
    }

    @Test
    void fakeGraphicsReportsSizeAndRecordsTitle() {
        FakeEngineServices services = new FakeEngineServices();
        FakeGraphics graphics = services.graphics();

        graphics.setSize(800, 600);
        assertEquals(800, graphics.getWidth());
        assertEquals(600, graphics.getHeight());
        graphics.setTitle("Snake — score: 3");
        assertEquals("Snake — score: 3", graphics.lastTitle());
    }
}
