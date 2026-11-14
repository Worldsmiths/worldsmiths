package io.github.worldsmiths.core;

/**
 * Entry points for engine and composed component registries. Games should wire
 * {@code SceneCodec.create(ComponentRegistry.combine(ComponentRegistries.builtins(), YourGeneratedComponentRegistry.INSTANCE))}
 * after generating their own registry with {@code -Aws.registry.package=...}.
 */
public final class ComponentRegistries {
    private ComponentRegistries() {}

    /** Built-in {@code @WsComponent} types shipped with ws-core ({@code Transform2D}, {@code Sprite}, …). */
    public static ComponentRegistry builtins() {
        return GeneratedComponentRegistry.INSTANCE;
    }
}
