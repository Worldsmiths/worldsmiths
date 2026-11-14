package io.github.worldsmiths.spike.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import io.github.worldsmiths.spike.game.generated.GeneratedComponentRegistry;
import io.github.worldsmiths.spike.runtime.Node;
import io.github.worldsmiths.spike.runtime.Scene;
import io.github.worldsmiths.spike.runtime.SceneJson;
import org.junit.jupiter.api.Test;

class SceneRoundTripTest {

    @Test
    void sceneRoundTripsThroughGeneratedRegistry() {
        Scene original = DemoScene.build();
        Json json = SceneJson.create(GeneratedComponentRegistry.INSTANCE);

        String text = json.toJson(original);
        Scene copy = json.fromJson(Scene.class, text);

        assertNotSame(original, copy);
        assertEquals("S2 demo", copy.name);
        assertEquals(2, copy.nodes.size);

        Node player = copy.nodes.get(0);
        assertEquals("Player", player.name);
        Transform2D t = player.get(Transform2D.class);
        assertEquals(12.5f, t.x);
        assertEquals(-3.25f, t.y);
        assertEquals(90f, t.rotation);
        assertEquals(1f, t.scaleX);
        Sprite s = player.get(Sprite.class);
        assertEquals("hero.png", s.texture);
        assertEquals(2, s.layer);
        assertEquals(0.75f, s.alpha);
        assertTrue(s.visible);

        Node shadow = player.children.get(0);
        assertEquals("Shadow", shadow.name);
        assertFalse(shadow.get(Sprite.class).visible);
        assertEquals(-1f, shadow.get(Transform2D.class).y);

        Node ground = copy.nodes.get(1);
        assertEquals(4f, ground.get(Transform2D.class).scaleX);
        assertEquals(1, ground.components.size);
    }

    @Test
    void componentsAreTaggedWithRegistryTagsNotClassNames() {
        String text = SceneJson.create(GeneratedComponentRegistry.INSTANCE).toJson(DemoScene.build());
        assertTrue(text.contains("\"type\":\"Transform2D\""), text);
        assertTrue(text.contains("\"type\":\"Sprite\""), text);
        assertFalse(text.contains("io.github"), text);
    }

    @Test
    void unknownComponentTagFailsWithoutFallingBackToClassForName() {
        Json json = SceneJson.create(GeneratedComponentRegistry.INSTANCE);
        String text = "{\"name\":\"x\",\"nodes\":[{\"name\":\"n\",\"components\":[{\"type\":\"java.lang.Object\"}]}]}";

        SerializationException e = assertThrows(SerializationException.class, () -> json.fromJson(Scene.class, text));
        assertTrue(e.getMessage().contains("Unknown component type 'java.lang.Object'"), e.getMessage());
    }

    @Test
    void verifyAcceptsTheDecodedSceneAndReportsTampering() {
        DemoScene.RoundTrip result = DemoScene.roundTrip();
        assertEquals(0, result.failures.size, result.failures.toString());

        Scene tampered = result.decoded;
        tampered.nodes.get(0).get(Sprite.class).texture = "villain.png";
        Array<String> failures = DemoScene.verify(tampered);
        assertEquals(1, failures.size, failures.toString());
        assertTrue(failures.first().contains("hero.png"), failures.first());
    }

    @Test
    void describeListsEveryNodeAndComponentValue() {
        String text = DemoScene.describe(DemoScene.roundTrip().decoded);
        assertTrue(text.contains("Player"), text);
        assertTrue(text.contains("Transform2D x=12.5 y=-3.25 rotation=90.0"), text);
        assertTrue(text.contains("Sprite texture=hero.png layer=2 alpha=0.75 visible=true"), text);
        assertTrue(text.contains("  Shadow"), text);
    }
}
