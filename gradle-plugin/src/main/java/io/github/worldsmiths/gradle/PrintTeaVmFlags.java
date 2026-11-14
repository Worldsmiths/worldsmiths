package io.github.worldsmiths.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/** Prints gdx-teavm reflection flags for TestKit / ADR-0007 verification. */
public abstract class PrintTeaVmFlags extends DefaultTask {

    @Input
    public abstract Property<Boolean> getReflectionEnabled();

    @Input
    public abstract Property<Boolean> getReflectionDefaults();

    @TaskAction
    public void print() {
        System.out.println("ws-teavm-reflectionEnabled=" + getReflectionEnabled().get());
        System.out.println("ws-teavm-reflectionDefaults=" + getReflectionDefaults().get());
    }
}
