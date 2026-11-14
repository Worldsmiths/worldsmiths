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
include("backends:web")
include("bom")
include("processor")
