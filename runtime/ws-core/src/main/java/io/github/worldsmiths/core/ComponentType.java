package io.github.worldsmiths.core;

import com.badlogic.gdx.utils.Json;

/**
 * One registered component: its JSON tag, class literal, factory and serializer, all produced at build
 * time. The serializer ctor parameter is opaque {@link Object} so game modules can generate
 * registries without putting LibGDX types in this public signature; {@link SceneCodec} casts
 * package-privately.
 */
public final class ComponentType<T> {
    private final String tag;
    private final Class<T> type;
    private final ComponentFactory<T> factory;
    private final Object serializer;

    /**
     * Used by generated {@code GeneratedComponentRegistry} code. Authors should not construct this;
     * use {@link SceneCodec} and {@link ComponentRegistry#combine}. Remains public so game modules can
     * generate registries outside {@code io.github.worldsmiths.core}.
     *
     * @param serializer opaque handle; generated code passes a GDX {@code Json.Serializer}
     */
    public ComponentType(String tag, Class<T> type, ComponentFactory<T> factory, Object serializer) {
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
    @SuppressWarnings("unchecked")
    Json.Serializer<T> serializer() {
        return (Json.Serializer<T>) serializer;
    }
}
