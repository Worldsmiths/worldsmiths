package io.github.worldsmiths.gradle;

import org.gradle.api.provider.Property;

/** The {@code targets.web {}} block in {@code settings.gradle.kts}. */
public abstract class WebTargetExtension {

    public abstract Property<String> getTitle();

    public abstract Property<Integer> getWidth();

    public abstract Property<Integer> getHeight();
}
