package io.github.worldsmiths.gradle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/**
 * Extracts {@code *.so} from libGDX {@code gdx-platform} natives jars into ABI folders
 * for AGP {@code jniLibs} (libGDX android template pattern).
 */
@CacheableTask
public abstract class ExtractAndroidNatives extends DefaultTask {

    @Classpath
    public abstract ConfigurableFileCollection getNativesJars();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void extract() {
        Path outputRoot = getOutputDir().get().getAsFile().toPath();
        clearDirectory(outputRoot);
        try {
            Files.createDirectories(outputRoot);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        for (java.io.File jar : getNativesJars().getFiles()) {
            String abi = abiFromJarName(jar.getName());
            if (abi == null) {
                continue;
            }
            Path abiDir = outputRoot.resolve(abi);
            try {
                Files.createDirectories(abiDir);
                extractSharedLibraries(jar.toPath(), abiDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    static String abiFromJarName(String jarName) {
        // e.g. gdx-platform-1.14.2-natives-arm64-v8a.jar
        int nativesAt = jarName.lastIndexOf("natives-");
        if (nativesAt < 0 || !jarName.endsWith(".jar")) {
            return null;
        }
        return jarName.substring(nativesAt + "natives-".length(), jarName.length() - ".jar".length());
    }

    private static void extractSharedLibraries(Path jar, Path abiDir) throws IOException {
        try (InputStream in = Files.newInputStream(jar);
                ZipInputStream zip = new ZipInputStream(in)) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                int slash = name.lastIndexOf('/');
                String fileName = slash >= 0 ? name.substring(slash + 1) : name;
                if (!fileName.endsWith(".so")) {
                    continue;
                }
                Path target = abiDir.resolve(fileName);
                try (OutputStream out = Files.newOutputStream(target)) {
                    zip.transferTo(out);
                }
            }
        }
    }

    private static void clearDirectory(Path root) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    if (!path.equals(root)) {
                        Files.deleteIfExists(path);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
