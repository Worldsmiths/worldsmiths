package io.github.worldsmiths.spike.runtime;

import com.badlogic.gdx.utils.Json;

/** One registered component: its JSON tag, class literal, factory and serializer, all produced at build time. */
public final class ComponentType<T> {
    private final String tag;
    private final Class<T> type;
    private final ComponentFactory<T> factory;
    private final Json.Serializer<T> serializer;

    public ComponentType(String tag, Class<T> type, ComponentFactory<T> factory, Json.Serializer<T> serializer) {
        this.tag = tag;
        this.type = type;
        this.factory = factory;
        this.serializer = serializer;
    }

    public String tag() {
        return tag;
    }

    public Class<T> type() {
        return type;
    }

    public T create() {
        return factory.create();
    }

    public Json.Serializer<T> serializer() {
        return serializer;
    }
}
