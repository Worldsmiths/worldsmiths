pluginManagement {
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

// Same composite pattern as the plugin TestKit: resolve published-looking coordinates
// from the monorepo via includeBuild + dependencySubstitution (ADR-0003).
includeBuild("../..") {
    dependencySubstitution {
        substitute(module("io.github.worldsmiths:backends-desktop"))
            .using(project(":backends:desktop"))
        substitute(module("io.github.worldsmiths:ws-core"))
            .using(project(":runtime:ws-core"))
    }
}

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
