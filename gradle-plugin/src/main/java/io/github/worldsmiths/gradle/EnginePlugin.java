package io.github.worldsmiths.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public final class EnginePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // No-op until the settings plugin spike. Applying it must not publish anything.
    }
}
