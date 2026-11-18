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
        assertNotNull(services.draw2d());
        assertNotNull(services.files());
        assertNotNull(services.thin3d());
        assertNotNull(services.extensions());
        assertSame(services.input(), services.input());
        assertSame(services.clock(), services.clock());
        assertSame(services.graphics(), services.graphics());
        assertSame(services.draw2d(), services.draw2d());
        assertSame(services.files(), services.files());
        assertSame(services.thin3d(), services.thin3d());
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

    @Test
    void fakeDraw2DRecordsOrthoColorLineRectAndDispose() {
        FakeEngineServices services = new FakeEngineServices();
        FakeDraw2D draw2d = services.draw2d();

        draw2d.setOrtho2D(0f, 0f, 640f, 480f);
        draw2d.begin(DrawMode.LINE);
        draw2d.setColor(0.25f, 0.25f, 0.28f, 1f);
        draw2d.line(0f, 0f, 32f, 0f);
        draw2d.rect(1f, 2f, 3f, 4f);
        draw2d.end();
        draw2d.dispose();

        assertEquals(0f, draw2d.lastOrthoX(), 1e-5f);
        assertEquals(0f, draw2d.lastOrthoY(), 1e-5f);
        assertEquals(640f, draw2d.lastOrthoWidth(), 1e-5f);
        assertEquals(480f, draw2d.lastOrthoHeight(), 1e-5f);
        assertTrue(draw2d.disposed());

        java.util.List<FakeDraw2D.Command> commands = draw2d.commands();
        assertEquals(FakeDraw2D.Command.Kind.ORTHO, commands.get(0).kind);
        assertEquals(FakeDraw2D.Command.Kind.BEGIN, commands.get(1).kind);
        assertEquals(DrawMode.LINE, commands.get(1).mode);
        assertEquals(FakeDraw2D.Command.Kind.COLOR, commands.get(2).kind);
        assertEquals(FakeDraw2D.Command.Kind.LINE, commands.get(3).kind);
        assertEquals(0f, commands.get(3).x1, 1e-5f);
        assertEquals(32f, commands.get(3).x2, 1e-5f);
        assertEquals(FakeDraw2D.Command.Kind.RECT, commands.get(4).kind);
        assertEquals(3f, commands.get(4).x2, 1e-5f);
        assertEquals(FakeDraw2D.Command.Kind.END, commands.get(5).kind);
        assertEquals(FakeDraw2D.Command.Kind.DISPOSE, commands.get(6).kind);
    }
}
