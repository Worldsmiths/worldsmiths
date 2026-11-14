package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Author-facing scene graph node: named container for components and child nodes.
 */
public final class Node {

    public String name;
    public final List<Object> components = new ArrayList<Object>();
    public final List<Node> children = new ArrayList<Node>();

    public Node() {}

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
            if (component.getClass() == type) {
                return (T) component;
            }
        }
        return null;
    }
}
