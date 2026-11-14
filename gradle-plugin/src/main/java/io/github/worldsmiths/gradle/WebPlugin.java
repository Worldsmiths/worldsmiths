package io.github.worldsmiths.gradle;

import com.github.xpenatan.gdx.teavm.gradle.GdxTeaVMExtension;
import com.github.xpenatan.gdx.teavm.gradle.GdxTeaVMGradlePlugin;
import java.util.Collections;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

/** Applied by the generated {@code :web} build script. */
public final class WebPlugin implements Plugin<Project> {

    /** Maven coordinate of the engine web backend; version matches the plugin. */
    static final String BACKENDS_WEB = EngineModuleCoordinates.BACKENDS_WEB_NOTATION;

    static final String TEA_VM_JS_BUILD = "gdx_teavm_web_js_build";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(GdxTeaVMGradlePlugin.class);
        WebExtension extension = project.getExtensions().create("worldsmithsWeb", WebExtension.class);

        TaskProvider<GenerateWebLauncher> generate = project.getTasks()
                .register("wsGenerateLauncher", GenerateWebLauncher.class, task -> {
                    task.getGameClass().set(extension.getGameClass());
                    task.getWidth().set(extension.getWidth());
                    task.getHeight().set(extension.getHeight());
                    task.getOutputDir().set(project.getLayout().getBuildDirectory().dir("generated/sources/ws-launcher"));
                });

        SourceSet main = project.getExtensions().getByType(JavaPluginExtension.class)
                .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        main.getJava().srcDir(generate.flatMap(GenerateWebLauncher::getOutputDir));

        project.getTasks().withType(JavaCompile.class).configureEach(compile -> {
            compile.getOptions().getRelease().set(17);
            compile.getOptions().getCompilerArgs().add("-Xlint:-options");
        });

        project.getDependencies().addProvider("implementation",
                extension.getGameModule().map(path ->
                        project.getDependencies().project(Collections.singletonMap("path", path))));
        project.getDependencies().add("implementation", BACKENDS_WEB);

        GdxTeaVMExtension teavm = project.getExtensions().getByType(GdxTeaVMExtension.class);
        teavm.getReflectionEnabled().set(false);
        teavm.getReflectionDefaults().set(false);
        teavm.assets(extension.getAssetsDir().map(path -> project.getRootProject().file(path)));
        teavm.webDefaults(defaults -> {
            defaults.getMainClass().set(GenerateWebLauncher.MAIN_CLASS);
            defaults.getHtmlTitle().set(extension.getTitle());
            defaults.getHtmlWidth().set(extension.getWidth());
            defaults.getHtmlHeight().set(extension.getHeight());
        });
        teavm.js(js -> {});
        teavm.wasm(wasm -> {});

        Provider<Directory> webappDir = project.getLayout().getBuildDirectory().dir("dist/js/webapp");
        project.getTasks().register(WsItchWebZip.TASK_NAME, WsItchWebZip.class, task -> {
            task.dependsOn(TEA_VM_JS_BUILD);
            task.getWebappDirectory().set(webappDir);
            task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("distributions"));
        });

        project.getTasks().register("wsPrintTeaVmFlags", PrintTeaVmFlags.class, task -> {
            task.setGroup("worldsmiths");
            task.setDescription("Print gdx-teavm reflection flags for verification");
            task.getReflectionEnabled().set(teavm.getReflectionEnabled());
            task.getReflectionDefaults().set(teavm.getReflectionDefaults());
        });

        // The extension is filled in by the generated script after apply() returns.
        // A module that sorts before :web, such as :core, is already evaluated by then.
        project.afterEvaluate(web ->
                GameModuleChecks.requireJava8WhenReady(web, extension.getGameModule().get()));
    }
}
