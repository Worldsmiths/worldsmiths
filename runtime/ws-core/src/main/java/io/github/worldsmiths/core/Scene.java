package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Author-facing root scene: a name plus an ordered list of top-level {@link Node}s.
 */
public final class Scene {

    public String name;
    public final List<Node> nodes = new ArrayList<Node>();

    public Scene() {}

    public Scene(String name) {
        this.name = name;
    }

    public Scene add(Node node) {
        nodes.add(node);
        return this;
    }
}
