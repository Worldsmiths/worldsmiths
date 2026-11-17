plugins {
    `java-library`
}

group = libs.versions.group.get()
version = libs.versions.engine.get()

// Plain JVM library: gdx-backend-robovm-metalangle / iosrobovm types must not sit on this
// compileClasspath. Depend on gdx core only; the generated :ios app wires MetalANGLE and
// calls IosBackend.attach.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options")
}

dependencies {
    api(project(":runtime:ws-core"))
    api(libs.gdx)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
