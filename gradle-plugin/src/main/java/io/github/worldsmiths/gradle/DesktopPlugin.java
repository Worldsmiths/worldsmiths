package io.github.worldsmiths.gradle;

import java.util.Collections;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

/** Applied by the generated {@code :desktop} build script. */
public final class DesktopPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(ApplicationPlugin.class);
        DesktopExtension extension = project.getExtensions().create("worldsmithsDesktop", DesktopExtension.class);

        TaskProvider<GenerateDesktopLauncher> generate = project.getTasks()
                .register("wsGenerateLauncher", GenerateDesktopLauncher.class, task -> {
                    task.getGameClass().set(extension.getGameClass());
                    task.getOutputDir().set(project.getLayout().getBuildDirectory().dir("generated/sources/ws-launcher"));
                });

        SourceSet main = project.getExtensions().getByType(JavaPluginExtension.class)
                .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        main.getJava().srcDir(generate.flatMap(GenerateDesktopLauncher::getOutputDir));

        project.getTasks().withType(JavaCompile.class).configureEach(compile -> {
            compile.getOptions().getRelease().set(8);
            compile.getOptions().getCompilerArgs().add("-Xlint:-options");
        });

        project.getDependencies().addProvider("implementation",
                extension.getGameModule().map(path ->
                        project.getDependencies().project(Collections.singletonMap("path", path))));

        project.getExtensions().getByType(JavaApplication.class).getMainClass()
                .set(GenerateDesktopLauncher.MAIN_CLASS);

        project.getTasks().register("wsPrintRuntimeClasspath", PrintRuntimeClasspath.class, task ->
                task.getClasspath().from(main.getRuntimeClasspath()));
        project.getTasks().register("wsRunDesktop", task -> {
            task.setGroup("worldsmiths");
            task.setDescription("Run the game on desktop");
            task.dependsOn("run");
        });

        // The extension is filled in by the generated script after apply() returns.
        // A module that sorts before :desktop, such as :core, is already evaluated by then.
        project.afterEvaluate(desktop -> {
            Project game = desktop.project(extension.getGameModule().get());
            if (game.getState().getExecuted()) {
                requireJava8(game);
            } else {
                game.afterEvaluate(DesktopPlugin::requireJava8);
            }
        });
    }

    /**
     * The launcher calls the game class directly and compiles at release 8, so a newer game
     * would build on desktop and fail on the other platforms. A module that does not compile
     * Java is left alone.
     */
    private static void requireJava8(Project game) {
        if (!game.getPluginManager().hasPlugin("java")) {
            return;
        }
        game.getTasks().withType(JavaCompile.class).forEach(compile -> {
            Integer release = compile.getOptions().getRelease().getOrNull();
            int level = release != null ? release.intValue() : javaMajor(compile.getTargetCompatibility());
            if (level > 8) {
                throw new GradleException("worldsmiths.gameModule '" + game.getPath()
                        + "' compiles for Java " + level
                        + ". Worldsmiths requires Java 8 so the same game can run on every platform.");
            }
        });
    }

    private static int javaMajor(String version) {
        String major = version.startsWith("1.") ? version.substring(2) : version;
        int dot = major.indexOf('.');
        if (dot >= 0) {
            major = major.substring(0, dot);
        }
        return Integer.parseInt(major);
    }
}
