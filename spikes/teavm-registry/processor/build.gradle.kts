plugins {
    java
}

// Build tooling: runs inside javac, so it may use Java 21 (ADR-0004).
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

dependencies {
    testImplementation(project(":runtime"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
