package io.github.worldsmiths.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class NodeTest {

    @Test
    void addStoresComponentRetrievableByExactClass() {
        Node node = new Node();
        MarkerComponent marker = new MarkerComponent();

        Node returned = node.add(marker);

        assertSame(node, returned);
        assertSame(marker, node.get(MarkerComponent.class));
        assertNull(node.get(OtherComponent.class));
    }

    @Test
    void addChildAppendsChildAndReturnsThis() {
        Node parent = new Node("parent");
        Node child = new Node("child");

        Node returned = parent.addChild(child);

        assertSame(parent, returned);
        assertEquals(Arrays.asList(child), parent.children);
    }

    private static final class MarkerComponent {}

    private static final class OtherComponent {}
}
