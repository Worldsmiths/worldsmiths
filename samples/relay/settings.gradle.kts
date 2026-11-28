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

rootProject.name = "relay"

include(":game")

worldsmiths {
    gameModule = ":game"
    gameClass = "io.github.worldsmiths.samples.relay.RelayGame"
    targets {
        desktop {
            title.set("Relay")
            width.set(800)
            height.set(600)
        }
        web {
            title.set("Relay")
            width.set(800)
            height.set(600)
        }
        android {
            title.set("Relay")
            width.set(800)
            height.set(600)
        }
        ios {
            title.set("Relay")
            width.set(800)
            height.set(600)
        }
    }
}
