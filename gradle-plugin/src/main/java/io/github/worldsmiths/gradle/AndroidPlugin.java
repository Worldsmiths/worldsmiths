package io.github.worldsmiths.gradle;

import com.android.build.api.dsl.ApplicationExtension;
import java.io.File;
import java.util.Collections;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

/** Applied by the generated {@code :android} build script. */
public final class AndroidPlugin implements Plugin<Project> {

    static final String BACKENDS_ANDROID = EngineModuleCoordinates.BACKENDS_ANDROID_NOTATION;
    static final String GDX_BACKEND_ANDROID =
            "com.badlogicgames.gdx:gdx-backend-android:1.14.2";

    static final String NAMESPACE = "io.github.worldsmiths.generated.android";

    static final String MISSING_GOOGLE_MESSAGE =
            "worldsmiths Android target requires Google Maven. Add google() to "
                    + "dependencyResolutionManagement { repositories { … } } "
                    + "(and pluginManagement repositories when applying AGP by id).";

    @Override
    public void apply(Project project) {
        if (!GoogleMavenRepositories.isPresent(project)) {
            throw new GradleException(MISSING_GOOGLE_MESSAGE);
        }

        // AGP 9 defaults enable built-in Kotlin; Worldsmiths launchers are Java-only.
        if (project.findProperty("android.builtInKotlin") == null) {
            project.getExtensions().getExtraProperties().set("android.builtInKotlin", "false");
        }
        if (project.findProperty("android.useAndroidX") == null) {
            project.getExtensions().getExtraProperties().set("android.useAndroidX", "true");
        }

        project.getPluginManager().apply("com.android.application");
        AndroidExtension extension =
                project.getExtensions().create("worldsmithsAndroid", AndroidExtension.class);

        ApplicationExtension android =
                project.getExtensions().getByType(ApplicationExtension.class);
        android.setNamespace(NAMESPACE);
        android.setCompileSdk(AndroidSdkPins.COMPILE_SDK);
        android.getDefaultConfig().setApplicationId(NAMESPACE);
        android.getDefaultConfig().setMinSdk(AndroidSdkPins.MIN_SDK);
        android.getDefaultConfig().setTargetSdk(AndroidSdkPins.TARGET_SDK);
        android.getCompileOptions().setSourceCompatibility(JavaVersion.VERSION_1_8);
        android.getCompileOptions().setTargetCompatibility(JavaVersion.VERSION_1_8);

        File buildDir = project.getLayout().getBuildDirectory().get().getAsFile();
        File manifestOut = new File(buildDir, "generated/ws-manifest/AndroidManifest.xml");
        File launcherOut = new File(buildDir, "generated/sources/ws-launcher");

        TaskProvider<GenerateAndroidManifest> generateManifest = project.getTasks()
                .register("wsGenerateManifest", GenerateAndroidManifest.class, task -> {
                    task.getTitle().set(extension.getTitle());
                    task.getOutputFile().set(manifestOut);
                });

        TaskProvider<GenerateAndroidLauncherActivity> generateLauncher = project.getTasks()
                .register("wsGenerateLauncher", GenerateAndroidLauncherActivity.class, task -> {
                    task.getGameClass().set(extension.getGameClass());
                    task.getOutputDir().set(launcherOut);
                });

        // Concrete dirs (not Providers): AGP 9 rejects Provider srcDir on the SourceSet API.
        // Task deps are wired via preBuild so generation still runs before compile.
        android.getSourceSets().named("main", sourceSet -> {
            sourceSet.getManifest().srcFile(manifestOut);
            sourceSet.getJava().srcDir(launcherOut);
        });

        project.getTasks().named("preBuild").configure(task -> {
            task.dependsOn(generateManifest);
            task.dependsOn(generateLauncher);
        });

        project.getDependencies().addProvider("implementation",
                extension.getGameModule().map(path ->
                        project.getDependencies().project(Collections.singletonMap("path", path))));
        project.getDependencies().add("implementation", BACKENDS_ANDROID);
        project.getDependencies().add("implementation", GDX_BACKEND_ANDROID);

        project.afterEvaluate(androidProject ->
                GameModuleChecks.requireJava8WhenReady(
                        androidProject, extension.getGameModule().get()));
    }
}
