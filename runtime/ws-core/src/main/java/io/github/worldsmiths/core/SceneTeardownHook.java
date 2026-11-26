package io.github.worldsmiths.core;

/**
 * Invoked when a scene exits during replace or {@link Scenes#unload()}. The teardown-hook list on
 * {@link Scenes} starts empty; ticket 14 owns ECS bake via this seam.
 */
public interface SceneTeardownHook {

    /** Called after exit lifecycle for {@code scene}, before current is cleared or replaced. */
    void onTeardown(Scene scene);
}
