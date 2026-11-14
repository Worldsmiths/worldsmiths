plugins {
    `java-library`
}

group = libs.versions.group.get()
version = libs.versions.engine.get()

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options")
}

dependencies {
    api(project(":runtime:ws-core"))
    api(libs.gdx)
    api(libs.gdx.backend.lwjgl3)
    api(libs.gdx.platform) {
        artifact {
            classifier = "natives-desktop"
        }
    }
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
