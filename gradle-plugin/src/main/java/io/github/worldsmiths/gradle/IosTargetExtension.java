package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code targets.ios {}} block in {@code settings.gradle.kts}. Empty block is enough to opt in. */
public abstract class IosTargetExtension {

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();
}
