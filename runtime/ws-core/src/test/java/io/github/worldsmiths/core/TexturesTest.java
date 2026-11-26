package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TexturesTest {

    @Test
    void fakePreloadRecordsCreateAndPreloadKindsWithPath() {
        FakeEngineServices services = new FakeEngineServices();
        FakeTextures textures = services.textures();
        textures.putTexture("hero.png");

        textures.preload("hero.png");

        java.util.List<FakeTextures.Command> commands = textures.commands();
        assertEquals(2, commands.size());
        assertEquals(FakeTextures.Command.Kind.CREATE, commands.get(0).kind);
        assertEquals("hero.png", commands.get(0).path);
        assertEquals(FakeTextures.Command.Kind.PRELOAD, commands.get(1).kind);
        assertEquals("hero.png", commands.get(1).path);
    }

    @Test
    void fakePreloadMissingKeyThrowsEngineExceptionWithPath() {
        FakeEngineServices services = new FakeEngineServices();
        FakeTextures textures = services.textures();

        EngineException thrown =
                assertThrows(EngineException.class, () -> textures.preload("missing.png"));
        assertTrue(thrown.getMessage().contains("missing.png"));
        assertTrue(textures.commands().isEmpty());
    }

    @Test
    void fakeSecondPreloadSameKeyIsCacheHitWithSingleCreate() {
        FakeEngineServices services = new FakeEngineServices();
        FakeTextures textures = services.textures();
        textures.putTexture("hero.png");

        textures.preload("hero.png");
        textures.preload("hero.png");

        java.util.List<FakeTextures.Command> commands = textures.commands();
        assertEquals(3, commands.size());
        assertEquals(FakeTextures.Command.Kind.CREATE, commands.get(0).kind);
        assertEquals(FakeTextures.Command.Kind.PRELOAD, commands.get(1).kind);
        assertEquals(FakeTextures.Command.Kind.PRELOAD, commands.get(2).kind);

        int creates = 0;
        for (FakeTextures.Command command : commands) {
            if (command.kind == FakeTextures.Command.Kind.CREATE) {
                creates++;
            }
        }
        assertEquals(1, creates);
    }

    @Test
    void fakeResolveRecordsCreateAndResolveKindsWithPath() {
        FakeEngineServices services = new FakeEngineServices();
        FakeTextures textures = services.textures();
        textures.putTexture("hero.png");

        textures.resolve("hero.png");

        java.util.List<FakeTextures.Command> commands = textures.commands();
        assertEquals(2, commands.size());
        assertEquals(FakeTextures.Command.Kind.CREATE, commands.get(0).kind);
        assertEquals("hero.png", commands.get(0).path);
        assertEquals(FakeTextures.Command.Kind.RESOLVE, commands.get(1).kind);
        assertEquals("hero.png", commands.get(1).path);
    }

    @Test
    void fakeResolveMissingKeyThrowsEngineExceptionWithPath() {
        FakeEngineServices services = new FakeEngineServices();
        FakeTextures textures = services.textures();

        EngineException thrown =
                assertThrows(EngineException.class, () -> textures.resolve("gone.png"));
        assertTrue(thrown.getMessage().contains("gone.png"));
        assertTrue(textures.commands().isEmpty());
    }

    @Test
    void fakeResolveThenPreloadSharesSingleCreate() {
        FakeEngineServices services = new FakeEngineServices();
        FakeTextures textures = services.textures();
        textures.putTexture("hero.png");

        textures.resolve("hero.png");
        textures.preload("hero.png");

        int creates = 0;
        for (FakeTextures.Command command : textures.commands()) {
            if (command.kind == FakeTextures.Command.Kind.CREATE) {
                creates++;
            }
        }
        assertEquals(1, creates);
    }

    @Test
    void fakeBagExposesStableTexturesInstance() {
        FakeEngineServices services = new FakeEngineServices();

        assertNotNull(services.textures());
        assertSame(services.textures(), services.textures());
    }
}
