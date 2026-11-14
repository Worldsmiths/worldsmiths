package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Transform2DTest {

    @Test
    void xyConstructorSetsPositionAndDefaultScale() {
        Transform2D transform = new Transform2D(5f, 7f);

        assertEquals(5f, transform.x);
        assertEquals(7f, transform.y);
        assertEquals(0f, transform.rotation);
        assertEquals(1f, transform.scaleX);
        assertEquals(1f, transform.scaleY);
    }
}
