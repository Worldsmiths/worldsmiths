package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class FakeScenesTest {

    private static final String SCENE_A_JSON =
            "{\"schemaVersion\":1,\"name\":\"A\",\"nodes\":[]}";
    private static final String SCENE_B_JSON =
            "{\"schemaVersion\":1,\"name\":\"B\",\"nodes\":[]}";

    @Test
    void loadAThenBRecordsExitAEnterBAndCurrentIsB() {
        FakeEngineServices services = new FakeEngineServices();
        services.files().putInternal("scenes/a.json", SCENE_A_JSON);
        services.files().putInternal("scenes/b.json", SCENE_B_JSON);
        FakeScenes scenes = services.scenes();
        scenes.setRegistry(ComponentRegistries.builtins());

        scenes.load("scenes/a.json");
        Scene sceneA = scenes.current();
        assertEquals("A", sceneA.name);

        scenes.load("scenes/b.json");

        assertEquals("B", scenes.current().name);
        assertSame(scenes.current(), scenes.current());

        List<FakeScenes.Command> commands = scenes.commands();
        assertTrue(containsExitFor(commands, sceneA), "expected EXIT for A: " + commands);
        assertTrue(containsEnterNamed(commands, "B"), "expected ENTER for B: " + commands);
    }

    @Test
    void pathLoadPreloadsDistinctSpriteKeysBeforeCurrentIsSet() {
        FakeFiles files = new FakeFiles();
        FakeTextures textures = new FakeTextures();
        files.putInternal(
                "scenes/textured.json",
                "{\"schemaVersion\":1,\"name\":\"Textured\",\"nodes\":["
                        + "{\"name\":\"hero\",\"components\":["
                        + "{\"type\":\"Transform2D\",\"x\":0.0,\"y\":0.0,\"rotation\":0.0,\"scaleX\":1.0,\"scaleY\":1.0},"
                        + "{\"type\":\"Sprite\",\"texture\":\"hero.png\",\"r\":1.0,\"g\":1.0,\"b\":1.0,\"a\":1.0,\"layer\":0,\"visible\":true}"
                        + "],\"children\":[]},"
                        + "{\"name\":\"coin\",\"components\":["
                        + "{\"type\":\"Transform2D\",\"x\":1.0,\"y\":0.0,\"rotation\":0.0,\"scaleX\":1.0,\"scaleY\":1.0},"
                        + "{\"type\":\"Sprite\",\"texture\":\"coin.png\",\"r\":1.0,\"g\":1.0,\"b\":1.0,\"a\":1.0,\"layer\":0,\"visible\":true}"
                        + "],\"children\":[]}"
                        + "]}");
        textures.putTexture("hero.png");
        textures.putTexture("coin.png");

        final AtomicReference<FakeScenes> scenesRef = new AtomicReference<FakeScenes>();
        final AtomicReference<Scene> currentDuringPreload = new AtomicReference<Scene>();
        Textures probing =
                new Textures() {
                    @Override
                    public void preload(String internalPath) {
                        FakeScenes scenes = scenesRef.get();
                        currentDuringPreload.set(scenes == null ? null : scenes.current());
                        textures.preload(internalPath);
                    }

                    @Override
                    public void resolve(String internalPath) {
                        textures.resolve(internalPath);
                    }
                };
        FakeScenes scenes = new FakeScenes(files, probing);
        scenesRef.set(scenes);
        scenes.setRegistry(ComponentRegistries.builtins());

        scenes.load("scenes/textured.json");

        assertNull(
                currentDuringPreload.get(),
                "current must still be unset while Sprite keys preload");
        assertEquals("Textured", scenes.current().name);

        Set<String> preloaded = new HashSet<String>();
        Set<String> created = new HashSet<String>();
        for (FakeTextures.Command cmd : textures.commands()) {
            if (cmd.kind == FakeTextures.Command.Kind.PRELOAD) {
                preloaded.add(cmd.path);
            }
            if (cmd.kind == FakeTextures.Command.Kind.CREATE) {
                created.add(cmd.path);
            }
        }
        assertEquals(setOf("hero.png", "coin.png"), preloaded);
        assertEquals(setOf("hero.png", "coin.png"), created);
        assertFalse(preloaded.contains(null));
    }

    @Test
    void unloadClearsCurrentAndSceneWalkYieldsNoSpriteCommands() {
        FakeEngineServices services = new FakeEngineServices();
        services.files().putInternal(
                "scenes/a.json",
                "{\"schemaVersion\":1,\"name\":\"A\",\"nodes\":["
                        + "{\"name\":\"hero\",\"components\":["
                        + "{\"type\":\"Transform2D\",\"x\":0.0,\"y\":0.0,\"rotation\":0.0,\"scaleX\":1.0,\"scaleY\":1.0},"
                        + "{\"type\":\"Sprite\",\"texture\":null,\"r\":1.0,\"g\":0.0,\"b\":0.0,\"a\":1.0,\"layer\":0,\"visible\":true}"
                        + "],\"children\":[]}"
                        + "]}");
        FakeScenes scenes = services.scenes();
        scenes.setRegistry(ComponentRegistries.builtins());

        scenes.load("scenes/a.json");
        assertFalse(SceneWalk.collectDrawCommands(scenes.current()).isEmpty());

        scenes.unload();

        assertNull(scenes.current());
        assertTrue(SceneWalk.collectDrawCommands(scenes.current()).isEmpty());
        assertTrue(
                containsKind(scenes.commands(), FakeScenes.Command.Kind.UNLOAD),
                "expected UNLOAD: " + scenes.commands());
        assertTrue(
                containsKind(scenes.commands(), FakeScenes.Command.Kind.EXIT),
                "expected EXIT on unload: " + scenes.commands());
    }

    @Test
    void failedLoadAfterAThrowsKeepsCurrentAAndRecordsFailKeep() {
        FakeEngineServices services = new FakeEngineServices();
        services.files().putInternal("scenes/a.json", SCENE_A_JSON);
        FakeScenes scenes = services.scenes();
        scenes.setRegistry(ComponentRegistries.builtins());

        scenes.load("scenes/a.json");
        Scene sceneA = scenes.current();
        assertEquals("A", sceneA.name);
        int commandsBeforeFail = scenes.commands().size();

        String missing = "scenes/missing.json";
        EngineException thrown = assertThrows(EngineException.class, () -> scenes.load(missing));
        assertTrue(thrown.getMessage().contains(missing), thrown.getMessage());
        assertSame(sceneA, scenes.current());

        List<FakeScenes.Command> commands = scenes.commands();
        assertTrue(
                containsFailKeep(commands, missing),
                "expected FAIL_KEEP_PREVIOUS for " + missing + ": " + commands);
        assertEquals(
                commandsBeforeFail + 2,
                commands.size(),
                "LOAD + FAIL_KEEP only; no EXIT/ENTER on fail: " + commands);
    }

    private static boolean containsExitFor(List<FakeScenes.Command> commands, Scene scene) {
        for (FakeScenes.Command cmd : commands) {
            if (cmd.kind == FakeScenes.Command.Kind.EXIT && cmd.scene == scene) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsEnterNamed(List<FakeScenes.Command> commands, String name) {
        for (FakeScenes.Command cmd : commands) {
            if (cmd.kind == FakeScenes.Command.Kind.ENTER
                    && cmd.scene != null
                    && name.equals(cmd.scene.name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsFailKeep(List<FakeScenes.Command> commands, String path) {
        for (FakeScenes.Command cmd : commands) {
            if (cmd.kind == FakeScenes.Command.Kind.FAIL_KEEP_PREVIOUS && path.equals(cmd.path)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsKind(List<FakeScenes.Command> commands, FakeScenes.Command.Kind kind) {
        for (FakeScenes.Command cmd : commands) {
            if (cmd.kind == kind) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> setOf(String... values) {
        Set<String> set = new HashSet<String>();
        for (String value : values) {
            set.add(value);
        }
        return set;
    }
}
