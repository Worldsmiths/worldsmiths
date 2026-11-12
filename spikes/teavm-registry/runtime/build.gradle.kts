plugins {
    `java-library`
}

// Engine runtime: Java 8 syntax, Java 7 APIs (ADR-0004).
tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options")
}

dependencies {
    api(libs.gdx)
}
