package io.github.worldsmiths.gradle;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

/**
 * Writes a generated module's files and returns their joined content.
 *
 * <p>Writing from plain plugin code would record "file absent" as a configuration-cache input, so the
 * next build would be invalidated by the file it created. A value source's own file access is not
 * tracked. Gradle re-runs it on every cache check and compares the returned value, so deleted
 * generated files are restored without discarding the cache.
 */
public abstract class GeneratedModuleSource implements ValueSource<String, GeneratedModuleSource.Params> {

    public interface Params extends ValueSourceParameters {
        Property<File> getDirectory();

        /** Relative path to file content. */
        MapProperty<String, String> getFiles();
    }

    @Override
    public String obtain() {
        Path dir = getParameters().getDirectory().get().toPath();
        StringBuilder all = new StringBuilder();
        getParameters().getFiles().get().forEach((relative, content) -> {
            writeIfChanged(dir.resolve(relative), content);
            all.append(relative).append('\n').append(content);
        });
        return all.toString();
    }

    private static void writeIfChanged(Path file, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try {
            if (Files.isRegularFile(file) && Arrays.equals(Files.readAllBytes(file), bytes)) {
                return;
            }
            Files.createDirectories(file.getParent());
            Files.write(file, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }
}
