package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Transform3DTest {

    @Test
    void xyzConstructorSetsPositionAndDefaultScale() {
        Transform3D transform = new Transform3D(5f, 7f, 9f);

        assertEquals(5f, transform.x);
        assertEquals(7f, transform.y);
        assertEquals(9f, transform.z);
        assertEquals(0f, transform.rotationX);
        assertEquals(0f, transform.rotationY);
        assertEquals(0f, transform.rotationZ);
        assertEquals(1f, transform.scale);
        assertEquals(1f, transform.r);
        assertEquals(1f, transform.g);
        assertEquals(1f, transform.b);
        assertEquals(1f, transform.a);
        assertTrue(transform.visible);
    }
}
