package io.github.worldsmiths.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Implemented by the generated {@code GeneratedComponentRegistry}. */
public interface ComponentRegistry {
    /**
     * Registered component types. Implementations must return a defensive copy (or otherwise
     * unmodifiable view); callers must not assume they can mutate the registry through this list.
     */
    List<ComponentType<?>> types();

    /**
     * Unions the given registries in order. Throws {@link IllegalArgumentException} if the same tag
     * appears more than once. Games typically combine {@link ComponentRegistries#builtins()} with
     * their module's generated registry before calling {@link SceneCodec#create(ComponentRegistry)}.
     */
    static ComponentRegistry combine(ComponentRegistry... registries) {
        if (registries == null || registries.length == 0) {
            throw new IllegalArgumentException("combine requires at least one registry");
        }
        final List<ComponentType<?>> merged = new ArrayList<ComponentType<?>>();
        Map<String, ComponentType<?>> byTag = new HashMap<String, ComponentType<?>>();
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
            public List<ComponentType<?>> types() {
                return Collections.unmodifiableList(new ArrayList<ComponentType<?>>(merged));
            }
        };
    }
}
