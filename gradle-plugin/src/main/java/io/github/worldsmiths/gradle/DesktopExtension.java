package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code worldsmithsDesktop {}} block in the generated desktop build script. */
public abstract class DesktopExtension {

    public abstract Property<String> getGameModule();

    public abstract Property<String> getGameClass();

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();
}
