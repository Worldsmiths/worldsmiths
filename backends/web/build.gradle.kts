plugins {
    `java-library`
}

group = libs.versions.group.get()
version = libs.versions.engine.get()

// gdx-teavm 1.6.2 backend jars are Java 17 bytecode (major 61), so this module
// cannot use --release 8 like backends/desktop. Match spike web launcher.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.compilerArgs.add("-Xlint:-options")
}

dependencies {
    api(project(":runtime:ws-core"))
    api(libs.gdx)
    api(libs.gdx.teavm.backend.web)
    api(libs.gdx.controllers.web)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
