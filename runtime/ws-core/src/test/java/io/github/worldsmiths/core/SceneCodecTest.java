package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.SerializationException;
import java.util.List;
import org.junit.jupiter.api.Test;

class SceneCodecTest {

    @Test
    void multiNodeSceneRoundTripsThroughGeneratedRegistry() {
        Scene original = buildDemoScene();
        SceneCodec codec = SceneCodec.create(ComponentRegistries.builtins());

        String text = codec.toJson(original);
        Scene copy = codec.fromJson(text);

        assertNotSame(original, copy);
        assertEquals("Demo", copy.name);
        assertEquals(2, copy.nodes.size());

        Node player = copy.nodes.get(0);
        assertEquals("Player", player.name);
        Transform2D t = player.get(Transform2D.class);
        assertEquals(12.5f, t.x);
        assertEquals(-3.25f, t.y);
        assertEquals(90f, t.rotation);
        assertEquals(1f, t.scaleX);
        assertEquals(1f, t.scaleY);
        Sprite s = player.get(Sprite.class);
        assertEquals("hero.png", s.texture);
        assertEquals(2, s.layer);
        assertEquals(0.75f, s.a);
        assertTrue(s.visible);

        Node shadow = player.children.get(0);
        assertEquals("Shadow", shadow.name);
        assertFalse(shadow.get(Sprite.class).visible);
        assertEquals(-1f, shadow.get(Transform2D.class).y);

        Node ground = copy.nodes.get(1);
        assertEquals("Ground", ground.name);
        assertEquals(4f, ground.get(Transform2D.class).scaleX);
        assertEquals(1, ground.components.size());
    }

    @Test
    void componentsAreTaggedWithRegistryTagsNotClassNames() {
        String text = SceneCodec.create(ComponentRegistries.builtins()).toJson(buildDemoScene());
        assertTrue(text.contains("\"type\":\"Transform2D\""), text);
        assertTrue(text.contains("\"type\":\"Sprite\""), text);
        assertFalse(text.contains("io.github"), text);
    }

    @Test
    void unknownComponentTagFailsWithoutClassForNameFallback() {
        SceneCodec codec = SceneCodec.create(ComponentRegistries.builtins());
        String text = "{\"schemaVersion\":1,\"name\":\"x\",\"nodes\":[{\"name\":\"n\",\"components\":[{\"type\":\"java.lang.Object\"}]}]}";

        SerializationException e = assertThrows(SerializationException.class, () -> codec.fromJson(text));
        assertTrue(e.getMessage().contains("Unknown component type 'java.lang.Object'"), e.getMessage());
    }

    @Test
    void writesSchemaVersionOne() {
        String text = SceneCodec.create(ComponentRegistries.builtins()).toJson(buildDemoScene());
        assertTrue(text.contains("\"schemaVersion\":1"), text);
        assertEquals(1, SceneCodec.SCHEMA_VERSION);
    }

    @Test
    void roundTripWalkSeesSameTextureKeysAndCellScaledLayout() {
        Transform2D tx = new Transform2D(3f, 5f);
        Sprite textured = new Sprite("hero.png");
        textured.r = 0.2f;
        textured.g = 0.4f;
        textured.b = 0.6f;
        textured.a = 0.8f;

        Sprite plain = new Sprite(null);
        plain.r = 1f;
        plain.g = 0f;
        plain.b = 0f;
        plain.a = 1f;

        Scene original =
                new Scene("WalkCouple")
                        .add(new Node("hero").add(tx).add(textured))
                        .add(new Node("plain").add(new Transform2D(1f, 0f)).add(plain));

        SceneCodec codec = SceneCodec.create(ComponentRegistries.builtins());
        String text = codec.toJson(original);
        assertTrue(text.contains("\"schemaVersion\":1"), text);
        Scene copy = codec.fromJson(text);

        List<RectDraw> draws = SceneWalk.collectDrawCommands(copy);
        assertEquals(2, draws.size());

        RectDraw heroDraw = draws.get(0);
        assertEquals("hero.png", heroDraw.textureKey);
        assertEquals(3f * SceneWalk.CELL_SIZE, heroDraw.x, 0.001f);
        assertEquals(5f * SceneWalk.CELL_SIZE, heroDraw.y, 0.001f);
        assertEquals(SceneWalk.CELL_SIZE, heroDraw.width, 0.001f);
        assertEquals(SceneWalk.CELL_SIZE, heroDraw.height, 0.001f);
        assertEquals(0.2f, heroDraw.r, 0.001f);
        assertEquals(0.4f, heroDraw.g, 0.001f);
        assertEquals(0.6f, heroDraw.b, 0.001f);
        assertEquals(0.8f, heroDraw.a, 0.001f);

        RectDraw plainDraw = draws.get(1);
        assertNull(plainDraw.textureKey);
        assertEquals(1f * SceneWalk.CELL_SIZE, plainDraw.x, 0.001f);
        assertEquals(1f, plainDraw.r, 0.001f);
    }

    @Test
    void missingSchemaVersionDefaultsToOne() {
        SceneCodec codec = SceneCodec.create(ComponentRegistries.builtins());
        String text = "{\"name\":\"Legacy\",\"nodes\":[]}";
        Scene scene = codec.fromJson(text);
        assertEquals("Legacy", scene.name);
        assertEquals(0, scene.nodes.size());
    }

    @Test
    void unknownSchemaVersionIsRejected() {
        SceneCodec codec = SceneCodec.create(ComponentRegistries.builtins());
        String text = "{\"schemaVersion\":99,\"name\":\"Future\",\"nodes\":[]}";
        SerializationException e = assertThrows(SerializationException.class, () -> codec.fromJson(text));
        assertTrue(e.getMessage().contains("Unsupported schemaVersion: 99"), e.getMessage());
    }

    private static Scene buildDemoScene() {
        Transform2D playerTx = new Transform2D(12.5f, -3.25f);
        playerTx.rotation = 90f;
        Sprite playerSprite = new Sprite("hero.png", 2);
        playerSprite.a = 0.75f;

        Node shadow = new Node("Shadow");
        Transform2D shadowTx = new Transform2D(0f, -1f);
        Sprite shadowSprite = new Sprite("shadow.png");
        shadowSprite.visible = false;
        shadow.add(shadowTx).add(shadowSprite);

        Node player = new Node("Player");
        player.add(playerTx).add(playerSprite).addChild(shadow);

        Node ground = new Node("Ground");
        Transform2D groundTx = new Transform2D();
        groundTx.scaleX = 4f;
        ground.add(groundTx);

        return new Scene("Demo").add(player).add(ground);
    }
}
