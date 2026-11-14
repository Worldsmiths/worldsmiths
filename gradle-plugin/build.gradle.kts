plugins {
    `java-gradle-plugin`
}

group = "io.github.worldsmiths"
version = "0.0.1-SNAPSHOT"

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation(gradleTestKit())
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("engine") {
            id = "io.github.worldsmiths.engine"
            implementationClass = "io.github.worldsmiths.gradle.WorldsmithsSettingsPlugin"
        }
        create("desktop") {
            id = "io.github.worldsmiths.desktop"
            implementationClass = "io.github.worldsmiths.gradle.DesktopPlugin"
        }
    }
}
