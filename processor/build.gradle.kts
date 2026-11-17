plugins {
    java
}

group = libs.versions.group.get()
version = libs.versions.engine.get()

// Build tooling: runs inside javac, so it may use Java 21 (ADR-0004).
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

dependencies {
    testImplementation(project(":runtime:ws-core"))
    // ws-core demoted gdx to implementation; processor tests still exercise generated Json.Serializer code.
    testImplementation(libs.gdx)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
