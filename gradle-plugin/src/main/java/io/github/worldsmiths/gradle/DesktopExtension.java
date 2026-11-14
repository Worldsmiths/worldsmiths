package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code worldsmithsDesktop {}} block in the generated desktop build script. */
public abstract class DesktopExtension {

    public abstract Property<String> getGameModule();

    public abstract Property<String> getGameClass();

    /** Assets directory relative to the settings / root project directory. */
    public abstract Property<String> getAssetsDir();

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();

    public abstract Property<Boolean> getVsync();

    public abstract Property<Boolean> getResizable();

    /**
     * Optional window icon path relative to {@link #getAssetsDir()}. Unset means no custom icon.
     */
    public abstract Property<String> getIcon();
}
