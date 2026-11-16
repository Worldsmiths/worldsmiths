package io.github.worldsmiths.gradle;

import com.android.build.api.dsl.ApplicationExtension;
import java.io.File;
import java.util.Collections;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskProvider;

/** Applied by the generated {@code :android} build script. */
public final class AndroidPlugin implements Plugin<Project> {

    static final String BACKENDS_ANDROID = EngineModuleCoordinates.BACKENDS_ANDROID_NOTATION;
    static final String GDX_VERSION = "1.14.2";
    static final String GDX_BACKEND_ANDROID =
            "com.badlogicgames.gdx:gdx-backend-android:" + GDX_VERSION;
    static final String GDX_PLATFORM = "com.badlogicgames.gdx:gdx-platform:" + GDX_VERSION;

    /** ABIs extracted from {@code gdx-platform} natives jars into {@code jniLibs}. */
    static final String[] NATIVES_ABIS = {
        "armeabi-v7a",
        "arm64-v8a",
        "x86",
        "x86_64"
    };

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
        File jniLibsOut = new File(buildDir, "ws-jniLibs");

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

        Configuration natives = project.getConfigurations().create("natives", configuration -> {
            configuration.setCanBeConsumed(false);
            configuration.setCanBeResolved(true);
            configuration.setVisible(false);
            configuration.setTransitive(false);
        });
        for (String abi : NATIVES_ABIS) {
            project.getDependencies().add("natives", GDX_PLATFORM + ":natives-" + abi);
        }

        TaskProvider<ExtractAndroidNatives> copyNatives = project.getTasks()
                .register("wsCopyAndroidNatives", ExtractAndroidNatives.class, task -> {
                    task.getNativesJars().from(natives);
                    task.getOutputDir().set(jniLibsOut);
                });

        // Concrete dirs (not Providers): AGP 9 rejects Provider srcDir on the SourceSet API.
        // Task deps are wired via preBuild so generation still runs before compile.
        android.getSourceSets().named("main", sourceSet -> {
            sourceSet.getManifest().srcFile(manifestOut);
            sourceSet.getJava().srcDir(launcherOut);
            sourceSet.getJniLibs().srcDir(jniLibsOut);
        });

        project.getTasks().named("preBuild").configure(task -> {
            task.dependsOn(generateManifest);
            task.dependsOn(generateLauncher);
            task.dependsOn(copyNatives);
        });
        project.getTasks().configureEach(task -> {
            String name = task.getName();
            if (name.contains("merge") && name.contains("JniLibFolders")) {
                task.dependsOn(copyNatives);
            }
        });

        project.getDependencies().addProvider("implementation",
                extension.getGameModule().map(path ->
                        project.getDependencies().project(Collections.singletonMap("path", path))));
        project.getDependencies().add("implementation", BACKENDS_ANDROID);
        project.getDependencies().add("implementation", GDX_BACKEND_ANDROID);

        // assetsDir is filled by the generated script after apply(); AGP needs a concrete File.
        // Missing / non-directory assetsDir is a soft no-op (desktop distZip empty-tree pattern).
        project.afterEvaluate(androidProject -> {
            String assetsPath = extension.getAssetsDir().get();
            File assetsDir = androidProject.getRootProject().file(assetsPath);
            if (assetsDir.isDirectory()) {
                android.getSourceSets().named("main", sourceSet ->
                        sourceSet.getAssets().srcDir(assetsDir));
            }
            GameModuleChecks.requireJava8WhenReady(
                    androidProject, extension.getGameModule().get());
        });
    }
}
