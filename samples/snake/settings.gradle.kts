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
        substitute(module("io.github.worldsmiths:ws-core"))
            .using(project(":runtime:ws-core"))
    }
}

rootProject.name = "snake"

include(":game")

worldsmiths {
    gameModule = ":game"
    gameClass = "io.github.worldsmiths.samples.snake.SnakeGame"
    targets {
        desktop {
            title.set("Snake")
            width.set(640)
            height.set(480)
        }
        web {
            title.set("Snake")
            width.set(640)
            height.set(480)
        }
        android {
            title.set("Snake")
            width.set(640)
            height.set(480)
        }
    }
}
