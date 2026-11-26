package io.github.worldsmiths.samples.sprites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.worldsmiths.core.FakeEngineServices;
import io.github.worldsmiths.core.FakeTextures;
import io.github.worldsmiths.core.RectDraw;
import io.github.worldsmiths.core.Scene;
import io.github.worldsmiths.core.SceneWalk;
import io.github.worldsmiths.core.Sprite;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Headless create path: FakeFiles + FakeTextures cover Files→SceneCodec→preload and textured draw
 * keys without a display or GDX in game sources.
 */
class SpritesGameTest {

    static final String DEMO_SCENE_JSON =
            "{\"schemaVersion\":1,\"name\":\"Sprites\",\"nodes\":["
                    + "{\"name\":\"hero\",\"components\":["
                    + "{\"type\":\"Transform2D\",\"x\":2.0,\"y\":3.0,\"rotation\":0.0,\"scaleX\":1.0,\"scaleY\":1.0},"
                    + "{\"type\":\"Sprite\",\"texture\":\"hero.png\",\"r\":1.0,\"g\":0.9,\"b\":0.8,\"a\":1.0,\"layer\":0,\"visible\":true}"
                    + "],\"children\":[]},"
                    + "{\"name\":\"coin\",\"components\":["
                    + "{\"type\":\"Transform2D\",\"x\":5.0,\"y\":1.0,\"rotation\":0.0,\"scaleX\":1.0,\"scaleY\":1.0},"
                    + "{\"type\":\"Sprite\",\"texture\":\"coin.png\",\"r\":1.0,\"g\":1.0,\"b\":0.2,\"a\":1.0,\"layer\":0,\"visible\":true}"
                    + "],\"children\":[]},"
                    + "{\"name\":\"plain\",\"components\":["
                    + "{\"type\":\"Transform2D\",\"x\":0.0,\"y\":0.0,\"rotation\":0.0,\"scaleX\":1.0,\"scaleY\":1.0},"
                    + "{\"type\":\"Sprite\",\"texture\":null,\"r\":0.2,\"g\":0.4,\"b\":0.8,\"a\":1.0,\"layer\":0,\"visible\":true}"
                    + "],\"children\":[]}"
                    + "]}";

    @Test
    void createLoadsSceneViaFilesAndSceneCodecWithTextureKeys() {
        FakeEngineServices services = new FakeEngineServices();
        services.files().putInternal("scenes/demo.json", DEMO_SCENE_JSON);
        services.textures().putTexture("hero.png");
        services.textures().putTexture("coin.png");

        SpritesGame game = new SpritesGame();
        Scene retained = game.createScene();
        game.create(services);

        assertEquals("Sprites", retained.name);
        assertEquals(3, retained.nodes.size());

        Sprite hero = retained.nodes.get(0).get(Sprite.class);
        assertNotNull(hero);
        assertEquals("hero.png", hero.texture);

        Sprite coin = retained.nodes.get(1).get(Sprite.class);
        assertNotNull(coin);
        assertEquals("coin.png", coin.texture);

        Sprite plain = retained.nodes.get(2).get(Sprite.class);
        assertNotNull(plain);
        assertTrue(plain.texture == null || plain.texture.isEmpty());
    }

    @Test
    void createPreloadsDistinctTextureKeysAndWalkDrawsThem() {
        FakeEngineServices services = new FakeEngineServices();
        services.files().putInternal("scenes/demo.json", DEMO_SCENE_JSON);
        services.textures().putTexture("hero.png");
        services.textures().putTexture("coin.png");

        SpritesGame game = new SpritesGame();
        Scene retained = game.createScene();
        game.create(services);

        FakeTextures textures = services.textures();
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

        List<RectDraw> draws = SceneWalk.collectDrawCommands(retained);
        assertEquals(3, draws.size());
        assertEquals("hero.png", draws.get(0).textureKey);
        assertEquals("coin.png", draws.get(1).textureKey);
        assertEquals(null, draws.get(2).textureKey);
        assertEquals(0.2f, draws.get(2).r, 0.001f);
    }

    private static Set<String> setOf(String a, String b) {
        Set<String> set = new HashSet<String>();
        set.add(a);
        set.add(b);
        return set;
    }
}
