pluginManagement {
    includeBuild("gradle-plugin")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "worldsmiths"

include("runtime:ws-core")
include("backends:desktop")
include("bom")
