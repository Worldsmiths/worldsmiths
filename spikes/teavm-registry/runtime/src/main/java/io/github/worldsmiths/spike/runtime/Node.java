package io.github.worldsmiths.spike.runtime;

import com.badlogic.gdx.utils.Array;

public final class Node {
    public String name;
    public final Array<Object> components = new Array<Object>();
    public final Array<Node> children = new Array<Node>();

    public Node() {
    }

    public Node(String name) {
        this.name = name;
    }

    public Node add(Object component) {
        components.add(component);
        return this;
    }

    public Node addChild(Node child) {
        children.add(child);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        for (Object component : components) {
            if (component.getClass() == type) return (T) component;
        }
        return null;
    }
}
