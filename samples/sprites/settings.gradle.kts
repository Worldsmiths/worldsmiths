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

// Resolve published-looking coordinates from the monorepo via bare includeBuild (ADR-0003).
// Project names match Maven artifact ids, so Gradle auto-substitutes — no dependencySubstitution.
includeBuild("../..")

rootProject.name = "sprites"

include(":game")

worldsmiths {
    gameModule = ":game"
    gameClass = "io.github.worldsmiths.samples.sprites.SpritesGame"
    targets {
        desktop {
            title.set("Sprites")
            width.set(640)
            height.set(480)
        }
        web {
            title.set("Sprites")
            width.set(640)
            height.set(480)
        }
    }
}
