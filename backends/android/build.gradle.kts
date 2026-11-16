plugins {
    `java-library`
}

group = libs.versions.group.get()
version = libs.versions.engine.get()

// Plain JVM library: gdx-backend-android is an AAR and cannot sit on this
// compileClasspath (no AGP / com.android.library here). Depend on gdx core only;
// the generated :android app wires the AAR and calls AndroidBackend.attach.
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
