package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code worldsmithsIos {}} block in the generated ios build script. */
public abstract class IosExtension {

    public abstract Property<String> getGameModule();

    public abstract Property<String> getGameClass();

    /** Assets directory relative to the settings / root project directory. */
    public abstract Property<String> getAssetsDir();

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();
}
