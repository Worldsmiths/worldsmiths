import org.robovm.gradle.RoboVMPluginExtension

buildscript {
    val robovmVersion: String by project
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.mobidevelop.robovm:robovm-gradle-plugin:$robovmVersion")
    }
}

apply(plugin = "robovm")

val gdxVersion: String by project
val robovmVersion: String by project

extra["mainClassName"] = "io.github.worldsmiths.spike.ios.IOSLauncher"

configure<RoboVMPluginExtension> {
    // No Apple signing identity exists, so CI builds an unsigned arm64 device IPA.
    isIosSkipSigning = true
    archs = "arm64"
}

listOf("createIPA", "robovmArchive", "robovmInstall").forEach { name ->
    tasks.named(name) { dependsOn("build") }
}

dependencies {
    "implementation"(project(":core"))
    "implementation"("com.mobidevelop.robovm:robovm-rt:$robovmVersion")
    "implementation"("com.mobidevelop.robovm:robovm-cocoatouch:$robovmVersion")
    // libGDX 1.12.0 to 1.14.2 use the MetalANGLE backend on iOS, as gdx-liftoff does.
    "implementation"("com.badlogicgames.gdx:gdx-backend-robovm-metalangle:$gdxVersion")
    "implementation"("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-ios")
}
