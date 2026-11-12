package io.github.worldsmiths.gradle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/** Writes the desktop entry point, which references the game class directly (no reflection). */
@CacheableTask
public abstract class GenerateDesktopLauncher extends DefaultTask {

    static final String MAIN_CLASS = "io.github.worldsmiths.generated.desktop.DesktopMain";

    @Input
    public abstract Property<String> getGameClass();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void generate() {
        Path source = getOutputDir().get().getAsFile().toPath()
                .resolve(MAIN_CLASS.replace('.', '/') + ".java");
        String code = "package io.github.worldsmiths.generated.desktop;\n"
                + "\n"
                + "public final class DesktopMain {\n"
                + "    public static void main(String[] args) {\n"
                + "        Object game = new " + getGameClass().get() + "();\n"
                + "        System.out.println(\"Worldsmiths desktop launched: \" + game);\n"
                + "    }\n"
                + "}\n";
        try {
            Files.createDirectories(source.getParent());
            Files.write(source, code.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
