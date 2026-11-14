package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code worldsmiths {}} block in {@code settings.gradle.kts}. */
public abstract class WorldsmithsExtension {

    /** Path of the user's game module, for example {@code ":core"}. */
    public abstract Property<String> getGameModule();

    /** Fully qualified game class the desktop launcher instantiates. */
    public abstract Property<String> getGameClass();
}
