pluginManagement {
    // AGP is on the engine plugin classpath (TeaVM-style); resolve via Google Maven even for iOS-only.
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
    }
}

// Phase-0 fixture: prove RoboVM-in-generated under configuration-cache (not Snake).
// Bare includeBuild — project names match Maven artifact ids (Gradle auto-substitutes).
includeBuild("../..")

rootProject.name = "ios-phase0"

include(":game")

worldsmiths {
    gameModule = ":game"
    gameClass = "io.github.worldsmiths.fixtures.iosphase0.Phase0Game"
    assetsDir = "assets"
    targets {
        desktop {}
        ios {
            title.set("iOS Phase-0")
            width.set(640)
            height.set(480)
        }
    }
}
