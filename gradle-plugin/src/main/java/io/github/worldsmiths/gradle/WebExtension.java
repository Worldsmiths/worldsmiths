package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code worldsmithsWeb {}} block in the generated web build script. */
public abstract class WebExtension {

    public abstract Property<String> getGameModule();

    public abstract Property<String> getGameClass();

    /** Assets directory relative to the settings / root project directory. */
    public abstract Property<String> getAssetsDir();

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();

    /** Browser storage prefix; defaults to gdx-teavm's {@code "app"}. */
    public abstract Property<String> getStoragePrefix();
}
