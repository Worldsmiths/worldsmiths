package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code targets.android {}} block in {@code settings.gradle.kts}. */
public abstract class AndroidTargetExtension {

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();
}
