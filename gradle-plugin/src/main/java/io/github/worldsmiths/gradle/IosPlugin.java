package io.github.worldsmiths.gradle;

import java.io.File;
import java.util.Collections;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.robovm.gradle.RoboVMPluginExtension;

/** Applied by the generated {@code :ios} build script. */
public final class IosPlugin implements Plugin<Project> {

    static final String BACKENDS_IOS = EngineModuleCoordinates.BACKENDS_IOS_NOTATION;

    static final String ROBOVM_RT =
            "com.mobidevelop.robovm:robovm-rt:" + IosPins.ROBOVM_VERSION;

    static final String ROBOVM_COCOATOUCH =
            "com.mobidevelop.robovm:robovm-cocoatouch:" + IosPins.ROBOVM_VERSION;

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        // TeaVM/AGP-style: RoboVM is on the gradle-plugin classpath; apply by legacy id.
        project.getPluginManager().apply("robovm");

        IosExtension extension = project.getExtensions().create("worldsmithsIos", IosExtension.class);

        File buildDir = project.getLayout().getBuildDirectory().get().getAsFile();
        File launcherOut = new File(buildDir, "generated/sources/ws-launcher");
        File robovmOut = new File(buildDir, "generated/ws-robovm");

        TaskProvider<GenerateIosDelegate> generateDelegate = project.getTasks()
                .register("wsGenerateDelegate", GenerateIosDelegate.class, task -> {
                    task.getGameClass().set(extension.getGameClass());
                    task.getWidth().set(extension.getWidth());
                    task.getHeight().set(extension.getHeight());
                    task.getOutputDir().set(launcherOut);
                });

        TaskProvider<GenerateIosRobovmConfig> generateRobovmConfig = project.getTasks()
                .register("wsGenerateRobovmConfig", GenerateIosRobovmConfig.class, task -> {
                    task.getMainClass().set(GenerateIosDelegate.MAIN_CLASS);
                    task.getTitle().set(extension.getTitle());
                    task.getAssetsDirAbsolute().set(extension.getAssetsDir().map(path -> {
                        File assetsDir = project.getRootProject().file(path);
                        return assetsDir.isDirectory() ? assetsDir.getAbsolutePath() : "";
                    }));
                    task.getOutputDir().set(robovmOut);
                });

        SourceSet main = project.getExtensions().getByType(JavaPluginExtension.class)
                .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        main.getJava().srcDir(generateDelegate.flatMap(GenerateIosDelegate::getOutputDir));

        project.getTasks().withType(JavaCompile.class).configureEach(compile -> {
            compile.getOptions().getRelease().set(8);
            compile.getOptions().getCompilerArgs().add("-Xlint:-options");
            compile.dependsOn(generateDelegate);
        });

        project.getDependencies().addProvider("implementation",
                extension.getGameModule().map(path ->
                        project.getDependencies().project(Collections.singletonMap("path", path))));
        project.getDependencies().add("implementation", BACKENDS_IOS);
        // MetalANGLE + natives-ios each ship META-INF/robovm/ios/robovm.xml; RoboVM merges those
        // into the app config (frameworks / frameworkPaths / libs). App robovm.xml from
        // wsGenerateRobovmConfig only adds assetsDir resources + base UIKit/OpenAL/Metal stack.
        project.getDependencies().add("implementation", IosPins.GDX_BACKEND_ROBOVM_METALANGLE);
        project.getDependencies().add("implementation", ROBOVM_RT);
        project.getDependencies().add("implementation", ROBOVM_COCOATOUCH);
        project.getDependencies().add(
                "implementation",
                IosPins.GDX_PLATFORM + ":" + IosPins.NATIVES_IOS_CLASSIFIER);

        RoboVMPluginExtension robovm =
                project.getExtensions().getByType(RoboVMPluginExtension.class);
        robovm.setConfigFile(new File(robovmOut, "robovm.xml").getAbsolutePath());
        robovm.setPropertiesFile(new File(robovmOut, "robovm.properties").getAbsolutePath());
        // Simulator-oriented Phase-0 / CI: unsigned arm64 install tree.
        robovm.setIosSkipSigning(true);
        robovm.setArchs("arm64");

        project.getTasks().named("robovmInstall").configure(task -> {
            task.dependsOn(generateDelegate);
            task.dependsOn(generateRobovmConfig);
            task.dependsOn(project.getTasks().named("classes"));
        });
        project.getTasks().named("robovmArchive").configure(task -> {
            task.dependsOn(generateDelegate);
            task.dependsOn(generateRobovmConfig);
            task.dependsOn(project.getTasks().named("classes"));
        });
        project.getTasks().configureEach(task -> {
            String name = task.getName();
            if (name.startsWith("launch") || name.equals("createIPA")) {
                task.dependsOn(generateDelegate);
                task.dependsOn(generateRobovmConfig);
            }
        });

        // RoboVM default install dir is build/robovm (ios-phase0 CI + snake-ios ship).
        project.getTasks().register(WsRobovmInstallZip.TASK_NAME, WsRobovmInstallZip.class, task -> {
            task.dependsOn("robovmInstall");
            task.getInstallDirectory().set(project.getLayout().getBuildDirectory().dir("robovm"));
            task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("distributions"));
        });

        // ext.mainClassName is also read by AbstractRoboVMTask when present.
        project.getExtensions().getExtraProperties().set("mainClassName", GenerateIosDelegate.MAIN_CLASS);

        project.afterEvaluate(ios ->
                GameModuleChecks.requireJava8WhenReady(ios, extension.getGameModule().get()));
    }
}
