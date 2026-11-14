package io.github.worldsmiths.core;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** Implemented by the generated {@code GeneratedComponentRegistry}. */
public interface ComponentRegistry {
    /**
     * Registered component types. Implementations must return a defensive copy (or otherwise
     * unmodifiable view); callers must not assume they can mutate the registry through this array.
     */
    Array<ComponentType<?>> types();

    /**
     * Unions the given registries in order. Throws {@link IllegalArgumentException} if the same tag
     * appears more than once. Games typically combine {@link ComponentRegistries#builtins()} with
     * their module's generated registry before calling {@link SceneCodec#create(ComponentRegistry)}.
     */
    static ComponentRegistry combine(ComponentRegistry... registries) {
        if (registries == null || registries.length == 0) {
            throw new IllegalArgumentException("combine requires at least one registry");
        }
        final Array<ComponentType<?>> merged = new Array<ComponentType<?>>();
        ObjectMap<String, ComponentType<?>> byTag = new ObjectMap<String, ComponentType<?>>();
        for (ComponentRegistry registry : registries) {
            if (registry == null) {
                throw new IllegalArgumentException("combine registries must not be null");
            }
            for (ComponentType<?> type : registry.types()) {
                ComponentType<?> previous = byTag.put(type.tag(), type);
                if (previous != null) {
                    throw new IllegalArgumentException(
                            "duplicate component tag '" + type.tag() + "' when combining registries");
                }
                merged.add(type);
            }
        }
        return new ComponentRegistry() {
            @Override
            public Array<ComponentType<?>> types() {
                return new Array<ComponentType<?>>(merged);
            }
        };
    }
}
