package io.github.worldsmiths.core;

import com.badlogic.gdx.utils.Json;

/** One registered component: its JSON tag, class literal, factory and serializer, all produced at build time. */
public final class ComponentType<T> {
    private final String tag;
    private final Class<T> type;
    private final ComponentFactory<T> factory;
    private final Json.Serializer<T> serializer;

    /**
     * Used by generated {@code GeneratedComponentRegistry} code. Authors should not construct this;
     * use {@link SceneCodec} and {@link ComponentRegistry#combine}. Remains public so game modules can
     * generate registries outside {@code io.github.worldsmiths.core}.
     */
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

    /** Package-private: only {@link SceneCodec} (same package) installs serializers on {@link Json}. */
    Json.Serializer<T> serializer() {
        return serializer;
    }
}
