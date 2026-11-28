plugins {
    `java-library`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options")
}

dependencies {
    implementation("io.github.worldsmiths:ws-core:0.0.1-SNAPSHOT")
    testImplementation("io.github.worldsmiths:backends-desktop:0.0.1-SNAPSHOT")
    testImplementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.14.2")
    testRuntimeOnly("com.badlogicgames.gdx:gdx-platform:1.14.2:natives-desktop")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

tasks.test {
    useJUnitPlatform()
    workingDir = layout.projectDirectory.dir("../assets").asFile
}