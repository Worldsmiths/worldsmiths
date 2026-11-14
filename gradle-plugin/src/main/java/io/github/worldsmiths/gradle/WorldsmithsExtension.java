package io.github.worldsmiths.gradle;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

/** The {@code worldsmiths {}} block in {@code settings.gradle.kts}. */
public abstract class WorldsmithsExtension {

    /** Path of the user's game module, for example {@code ":core"}. */
    public abstract Property<String> getGameModule();

    /** Fully qualified game class the desktop launcher instantiates. */
    public abstract Property<String> getGameClass();

    /**
     * Game assets directory relative to the settings root. Defaults to {@code "assets"}.
     * Wired into desktop run/dist and gdx-teavm {@code assets()}.
     */
    public abstract Property<String> getAssetsDir();

    @Nested
    public abstract TargetsExtension getTargets();

    public void targets(Action<? super TargetsExtension> action) {
        action.execute(getTargets());
    }
}
