package io.github.worldsmiths.gradle;

import java.util.Collections;
import java.util.Locale;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

/** Applied by the generated {@code :desktop} build script. */
public final class DesktopPlugin implements Plugin<Project> {

    /** Maven coordinate of the engine desktop backend; version matches the plugin. */
    static final String BACKENDS_DESKTOP = EngineModuleCoordinates.BACKENDS_DESKTOP_NOTATION;

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(ApplicationPlugin.class);
        DesktopExtension extension = project.getExtensions().create("worldsmithsDesktop", DesktopExtension.class);

        TaskProvider<GenerateDesktopLauncher> generate = project.getTasks()
                .register("wsGenerateLauncher", GenerateDesktopLauncher.class, task -> {
                    task.getGameClass().set(extension.getGameClass());
                    task.getTitle().set(extension.getTitle());
                    task.getWidth().set(extension.getWidth());
                    task.getHeight().set(extension.getHeight());
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
        project.getDependencies().add("implementation", BACKENDS_DESKTOP);

        project.getExtensions().getByType(JavaApplication.class).getMainClass()
                .set(GenerateDesktopLauncher.MAIN_CLASS);

        if (isMacOs()) {
            project.getTasks().named(ApplicationPlugin.TASK_RUN_NAME, JavaExec.class, run ->
                    run.jvmArgs("-XstartOnFirstThread"));
        }

        project.getTasks().register("wsPrintRuntimeClasspath", PrintRuntimeClasspath.class, task ->
                task.getClasspath().from(main.getRuntimeClasspath()));
        project.getTasks().register("wsRunDesktop", task -> {
            task.setGroup("worldsmiths");
            task.setDescription("Run the game on desktop");
            task.dependsOn("run");
        });

        // The extension is filled in by the generated script after apply() returns.
        // A module that sorts before :desktop, such as :core, is already evaluated by then.
        project.afterEvaluate(desktop ->
                GameModuleChecks.requireJava8WhenReady(desktop, extension.getGameModule().get()));
    }

    private static boolean isMacOs() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase(Locale.ROOT).startsWith("mac");
    }
}
