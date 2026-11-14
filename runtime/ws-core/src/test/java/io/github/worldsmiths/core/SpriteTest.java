package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpriteTest {

    @Test
    void textureConstructorSetsDefaultsForColorLayerAndVisibility() {
        Sprite sprite = new Sprite("snake/head.png");

        assertEquals("snake/head.png", sprite.texture);
        assertEquals(1f, sprite.r);
        assertEquals(1f, sprite.g);
        assertEquals(1f, sprite.b);
        assertEquals(1f, sprite.a);
        assertEquals(0, sprite.layer);
        assertTrue(sprite.visible);
    }
}
