package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code targets.desktop {}} block in {@code settings.gradle.kts}. */
public abstract class DesktopTargetExtension {

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();

    /** Whether the window uses vsync. Defaults to {@code true}. */
    public abstract Property<Boolean> getVsync();

    /** Whether the window is resizable. Defaults to {@code true}. */
    public abstract Property<Boolean> getResizable();

    /**
     * Optional window icon path relative to {@code worldsmiths.assetsDir} (desktop run working
     * directory / shipped assets root). Unset means no custom icon.
     */
    public abstract Property<String> getIcon();
}
