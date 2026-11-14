package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.SerializationException;
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
