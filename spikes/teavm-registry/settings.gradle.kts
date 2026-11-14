// Standalone build: the root worldsmiths build does not include this directory.
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "teavm-registry-spike"

include("runtime", "processor", "game", "web")
