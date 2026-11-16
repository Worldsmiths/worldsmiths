package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code worldsmithsAndroid {}} block in the generated android build script. */
public abstract class AndroidExtension {

    public abstract Property<String> getGameModule();

    public abstract Property<String> getGameClass();

    /** Assets directory relative to the settings / root project directory. */
    public abstract Property<String> getAssetsDir();

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();
}
