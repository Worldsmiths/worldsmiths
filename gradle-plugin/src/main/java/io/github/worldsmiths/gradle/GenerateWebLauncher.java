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

/** Writes the web entry point, which references the game class directly (no reflection). */
@CacheableTask
public abstract class GenerateWebLauncher extends DefaultTask {

    static final String MAIN_CLASS = "io.github.worldsmiths.generated.web.WebMain";

    @Input
    public abstract Property<String> getGameClass();

    @Input
    public abstract Property<Integer> getWidth();

    @Input
    public abstract Property<Integer> getHeight();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void generate() {
        Path source = getOutputDir().get().getAsFile().toPath()
                .resolve(MAIN_CLASS.replace('.', '/') + ".java");
        String gameClass = getGameClass().get();
        int width = getWidth().get();
        int height = getHeight().get();
        String code = "package io.github.worldsmiths.generated.web;\n"
                + "\n"
                + "import io.github.worldsmiths.core.WorldsmithsGame;\n"
                + "import io.github.worldsmiths.web.WebBackend;\n"
                + "import io.github.worldsmiths.web.WebConfig;\n"
                + "\n"
                + "public final class WebMain {\n"
                + "    public static void main(String[] args) {\n"
                + "        WorldsmithsGame game = new " + gameClass + "();\n"
                + "        WebBackend.start(game, new WebConfig("
                + width + ", " + height + "));\n"
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
