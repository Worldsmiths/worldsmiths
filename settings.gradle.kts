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
include("backends-desktop")
project(":backends-desktop").projectDir = file("backends/desktop")
include("backends-web")
project(":backends-web").projectDir = file("backends/web")
include("backends-android")
project(":backends-android").projectDir = file("backends/android")
include("backends-ios")
project(":backends-ios").projectDir = file("backends/ios")
include("bom")
include("processor")
