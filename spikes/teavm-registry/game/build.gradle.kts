plugins {
    `java-library`
}

// Game code: Java 8 syntax, Java 7 APIs (ADR-0004). This is the code TeaVM compiles.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.compilerArgs.addAll(listOf("-Xlint:-options", "-Aws.registry.package=io.github.worldsmiths.spike.game.generated"))
}

dependencies {
    api(project(":runtime"))
    annotationProcessor(project(":processor"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    systemProperty("spike.root", rootDir.absolutePath)
}
