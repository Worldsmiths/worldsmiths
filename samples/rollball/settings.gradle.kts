pluginManagement {
    // AGP lives on the engine plugin classpath (TeaVM-style); Plugin Portal alone cannot resolve it.
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
        google()
        mavenCentral()
    }
}

// Same composite pattern as the plugin TestKit: resolve published-looking coordinates
// from the monorepo via includeBuild + dependencySubstitution (ADR-0003).
includeBuild("../..") {
    dependencySubstitution {
        substitute(module("io.github.worldsmiths:backends-desktop"))
            .using(project(":backends:desktop"))
        substitute(module("io.github.worldsmiths:backends-web"))
            .using(project(":backends:web"))
        substitute(module("io.github.worldsmiths:backends-android"))
            .using(project(":backends:android"))
        substitute(module("io.github.worldsmiths:backends-ios"))
            .using(project(":backends:ios"))
        substitute(module("io.github.worldsmiths:ws-core"))
            .using(project(":runtime:ws-core"))
    }
}

rootProject.name = "rollball"

include(":game")

worldsmiths {
    gameModule = ":game"
    gameClass = "io.github.worldsmiths.samples.rollball.RollballGame"
    targets {
        desktop {
            title.set("Rollball")
            width.set(800)
            height.set(600)
        }
        web {
            title.set("Rollball")
            width.set(800)
            height.set(600)
        }
        android {
            title.set("Rollball")
            width.set(800)
            height.set(600)
        }
        ios {
            title.set("Rollball")
            width.set(800)
            height.set(600)
        }
    }
}
