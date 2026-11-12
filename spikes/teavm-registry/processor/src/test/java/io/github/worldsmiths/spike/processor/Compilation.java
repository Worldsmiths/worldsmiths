package io.github.worldsmiths.spike.processor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/** Compiles sources on disk with {@link WsComponentProcessor} and the test classpath. */
final class Compilation {
    final boolean success;
    final List<Diagnostic<? extends JavaFileObject>> diagnostics;
    final Path classes;
    final Path generated;

    private Compilation(boolean success, List<Diagnostic<? extends JavaFileObject>> diagnostics, Path classes, Path generated) {
        this.success = success;
        this.diagnostics = diagnostics;
        this.classes = classes;
        this.generated = generated;
    }

    static Compilation compile(Path workDir, Map<String, String> sources, String... options) {
        try {
            Path src = Files.createDirectories(workDir.resolve("src"));
            Path classes = Files.createDirectories(workDir.resolve("classes"));
            Path generated = Files.createDirectories(workDir.resolve("generated"));
            List<Path> files = new ArrayList<>();
            for (Map.Entry<String, String> e : sources.entrySet()) {
                Path file = src.resolve(e.getKey().replace('.', '/') + ".java");
                Files.createDirectories(file.getParent());
                Files.writeString(file, e.getValue());
                files.add(file);
            }
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
                List<String> args = new ArrayList<>(List.of(
                        "--release", "8", "-Xlint:-options",
                        "-classpath", System.getProperty("java.class.path"),
                        "-d", classes.toString(),
                        "-s", generated.toString()));
                args.addAll(List.of(options));
                JavaCompiler.CompilationTask task = compiler.getTask(null, fm, diagnostics, args, null,
                        fm.getJavaFileObjectsFromPaths(files));
                task.setProcessors(List.of(new WsComponentProcessor()));
                boolean ok = task.call();
                return new Compilation(ok, diagnostics.getDiagnostics(), classes, generated);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String errors() {
        return diagnostics.stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .map(d -> d.getMessage(Locale.ROOT))
                .collect(Collectors.joining("\n"));
    }

    String generatedSource(String qualifiedName) {
        try {
            return Files.readString(generated.resolve(qualifiedName.replace('.', '/') + ".java"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    ClassLoader classLoader() {
        try {
            return new URLClassLoader(new URL[] {classes.toUri().toURL()}, getClass().getClassLoader());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
