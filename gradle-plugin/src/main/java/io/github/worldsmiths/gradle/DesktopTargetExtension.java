package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code targets.desktop {}} block in {@code settings.gradle.kts}. */
public abstract class DesktopTargetExtension {

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();
}
