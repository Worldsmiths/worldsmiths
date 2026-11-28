package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EngineServicesTest {

    @Test
    void fakeBagExposesNonNullFloorServicesAndExtensions() {
        FakeEngineServices services = new FakeEngineServices();

        assertNotNull(services.input());
        assertNotNull(services.actions());
        assertNotNull(services.clock());
        assertNotNull(services.graphics());
        assertNotNull(services.draw2d());
        assertNotNull(services.files());
        assertNotNull(services.d3());
        assertNotNull(services.audio());
        assertNotNull(services.ui());
        assertNotNull(services.textures());
        assertNotNull(services.uiKit());
        assertNotNull(services.log());
        assertNotNull(services.assets());
        assertNotNull(services.scenes());
        assertNotNull(services.extensions());
        assertSame(services.input(), services.input());
        assertSame(services.actions(), services.actions());
        assertSame(services.clock(), services.clock());
        assertSame(services.graphics(), services.graphics());
        assertSame(services.draw2d(), services.draw2d());
        assertSame(services.files(), services.files());
        assertSame(services.d3(), services.d3());
        assertSame(services.audio(), services.audio());
        assertSame(services.ui(), services.ui());
        assertSame(services.textures(), services.textures());
        assertSame(services.uiKit(), services.uiKit());
        assertSame(services.log(), services.log());
        assertSame(services.assets(), services.assets());
        assertSame(services.scenes(), services.scenes());
        assertTrue(services.actions() instanceof FakeActions);
        assertTrue(services.assets() instanceof FakeAssets);
        assertTrue(services.scenes() instanceof FakeScenes);
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

    @Test
    void fakeAudioFailClosedMissingCreateSound() {
        FakeEngineServices services = new FakeEngineServices();
        FakeAudio audio = services.audio();

        EngineException thrown =
                assertThrows(EngineException.class, () -> audio.createSound("missing.wav"));
        assertTrue(thrown.getMessage().contains("missing.wav"));
        assertTrue(audio.commands().isEmpty());
    }

    @Test
    void fakeAudioRecordsCreatePlayStopDisposeKinds() {
        FakeEngineServices services = new FakeEngineServices();
        FakeAudio audio = services.audio();
        audio.putSound("audio/cue.wav");

        Sound sound = audio.createSound("audio/cue.wav");
        sound.play();
        sound.stop();
        sound.dispose();
        sound.dispose();

        java.util.List<FakeAudio.Command> commands = audio.commands();
        assertEquals(4, commands.size());
        assertEquals(FakeAudio.Command.Kind.CREATE_SOUND, commands.get(0).kind);
        assertEquals("audio/cue.wav", commands.get(0).path);
        assertEquals(FakeAudio.Command.Kind.PLAY, commands.get(1).kind);
        assertEquals(FakeAudio.Command.Kind.STOP, commands.get(2).kind);
        assertEquals(FakeAudio.Command.Kind.DISPOSE, commands.get(3).kind);
    }

    @Test
    void fakeUiFailClosedMissingCreateFont() {
        FakeEngineServices services = new FakeEngineServices();
        FakeUi ui = services.ui();

        EngineException thrown =
                assertThrows(EngineException.class, () -> ui.createFont("missing.fnt"));
        assertTrue(thrown.getMessage().contains("missing.fnt"));
        assertTrue(ui.commands().isEmpty());
    }

    @Test
    void fakeUiRecordsCreateDrawDisposeKinds() {
        FakeEngineServices services = new FakeEngineServices();
        FakeUi ui = services.ui();
        ui.putFont("fonts/hud.fnt");

        Font font = ui.createFont("fonts/hud.fnt");
        font.drawString("1:23", 8f, 24f);
        font.dispose();
        font.dispose();

        java.util.List<FakeUi.Command> commands = ui.commands();
        assertEquals(3, commands.size());
        assertEquals(FakeUi.Command.Kind.CREATE_FONT, commands.get(0).kind);
        assertEquals("fonts/hud.fnt", commands.get(0).path);
        assertEquals(FakeUi.Command.Kind.DRAW_STRING, commands.get(1).kind);
        assertEquals("1:23", commands.get(1).text);
        assertEquals(8f, commands.get(1).x, 1e-5f);
        assertEquals(24f, commands.get(1).y, 1e-5f);
        assertEquals(FakeUi.Command.Kind.DISPOSE, commands.get(2).kind);
    }
}
