package io.github.worldsmiths.spike.runtime;

import com.badlogic.gdx.utils.Array;

public final class Scene {
    public String name;
    public final Array<Node> nodes = new Array<Node>();

    public Scene() {
    }

    public Scene(String name) {
        this.name = name;
    }

    public Scene add(Node node) {
        nodes.add(node);
        return this;
    }
}
