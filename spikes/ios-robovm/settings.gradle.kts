// Standalone build for spike S3. It is deliberately not included from the root build.
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "spike-ios-robovm"

include("core", "ios")
