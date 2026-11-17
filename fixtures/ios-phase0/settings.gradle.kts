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
includeBuild("../..") {
    dependencySubstitution {
        substitute(module("io.github.worldsmiths:backends-desktop"))
            .using(project(":backends:desktop"))
        substitute(module("io.github.worldsmiths:backends-ios"))
            .using(project(":backends:ios"))
        substitute(module("io.github.worldsmiths:ws-core"))
            .using(project(":runtime:ws-core"))
    }
}

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
