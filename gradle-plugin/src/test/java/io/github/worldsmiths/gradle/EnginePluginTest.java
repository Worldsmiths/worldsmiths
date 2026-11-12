package io.github.worldsmiths.gradle;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class EnginePluginTest {

    @Test
    void applyingThePluginAddsNoPublishTask() {
        org.gradle.api.Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(EnginePlugin.class);
        assertNull(project.getTasks().findByName("publish"));
    }
}
