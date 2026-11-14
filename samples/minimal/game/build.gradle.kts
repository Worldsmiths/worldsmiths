plugins {
    `java-library`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options")
}

dependencies {
    implementation("io.github.worldsmiths:ws-core:0.0.1-SNAPSHOT")
}
