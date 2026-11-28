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
        maven { url = uri("https://jitpack.io") }
    }
}

// Resolve published-looking coordinates from the monorepo via bare includeBuild (ADR-0003).
// Project names match Maven artifact ids, so Gradle auto-substitutes — no dependencySubstitution.
includeBuild("../..")

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
        ios {
            title.set("Snake")
            width.set(640)
            height.set(480)
        }
    }
}
