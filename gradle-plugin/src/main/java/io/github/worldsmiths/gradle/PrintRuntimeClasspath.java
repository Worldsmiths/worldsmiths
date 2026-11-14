package io.github.worldsmiths.gradle;

import java.io.File;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

/** Prints one {@code ws-classpath: <file name>} line per runtime classpath entry. */
@DisableCachingByDefault(because = "Prints diagnostics only")
public abstract class PrintRuntimeClasspath extends DefaultTask {

    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @TaskAction
    public void print() {
        for (File file : getClasspath().getFiles()) {
            System.out.println("ws-classpath: " + file.getName());
        }
    }
}
