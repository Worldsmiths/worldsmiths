pluginManagement {
    // AGP lives on the engine plugin classpath even for desktop-only samples.
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("../../gradle-plugin")
}

plugins {
    id("io.github.worldsmiths.engine")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// Resolve published-looking coordinates from the monorepo via bare includeBuild (ADR-0003).
// Project names match Maven artifact ids, so Gradle auto-substitutes — no dependencySubstitution.
includeBuild("../..")

rootProject.name = "minimal"

include(":game")

worldsmiths {
    gameModule = ":game"
    gameClass = "io.github.worldsmiths.samples.minimal.MinimalGame"
    targets {
        desktop {
            title.set("Minimal")
            width.set(800)
            height.set(600)
        }
    }
}
