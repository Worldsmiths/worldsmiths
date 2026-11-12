import org.robovm.gradle.RoboVMPluginExtension
import org.robovm.gradle.tasks.IPhoneSimulatorTask

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.mobidevelop.robovm:robovm-gradle-plugin:${project.property("robovmVersion")}")
    }
}

apply(plugin = "robovm")

val gdxVersion = providers.gradleProperty("gdxVersion").get()
val robovmVersion = providers.gradleProperty("robovmVersion").get()

extra["mainClassName"] = "io.github.worldsmiths.spike.ios.IOSLauncher"

configure<RoboVMPluginExtension> {
    // RoboVM 3 dropped the Harmony security providers that the 2.3 config force-links.
    if (robovmVersion.startsWith("3.")) {
        configFile = file("robovm-3.xml").absolutePath
    }
    // No Apple signing identity exists, so CI builds an unsigned arm64 device IPA.
    isIosSkipSigning = true
    archs = "arm64"
    arch = "arm64"
}

listOf("createIPA", "robovmArchive", "robovmInstall", "launchIPhoneSimulator").forEach { name ->
    tasks.named(name) { dependsOn("build") }
}

tasks.named<IPhoneSimulatorTask>("launchIPhoneSimulator") {
    setArgs("--s3-probes")
}

dependencies {
    "implementation"(project(":core"))
    "implementation"("com.mobidevelop.robovm:robovm-rt:$robovmVersion")
    "implementation"("com.mobidevelop.robovm:robovm-cocoatouch:$robovmVersion")
    // libGDX 1.12.0 to 1.14.2 use the MetalANGLE backend on iOS, as gdx-liftoff does.
    "implementation"("com.badlogicgames.gdx:gdx-backend-robovm-metalangle:$gdxVersion")
    "implementation"("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-ios")
}
