plugins {
    `java-library`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options")
}

dependencies {
    implementation("io.github.worldsmiths:ws-core:0.0.1-SNAPSHOT")
    // LibGDX core only (includes g3d ModelBatch). Backends stay in the platform modules.
    implementation("com.badlogicgames.gdx:gdx:1.14.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

tasks.test {
    useJUnitPlatform()
}
