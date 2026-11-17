plugins {
    `java-gradle-plugin`
}

group = "io.github.worldsmiths"
version = "0.0.1-SNAPSHOT"

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

dependencies {
    implementation("com.github.xpenatan.gdx-teavm:gdx-teavm-plugin:1.6.2")
    // TeaVM-style: AGP on the plugin classpath so games do not declare it.
    implementation("com.android.tools.build:gradle:9.0.0")
    // TeaVM/AGP-style: RoboVM on the plugin classpath so games do not declare it.
    implementation("com.mobidevelop.robovm:robovm-gradle-plugin:2.3.27")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation(gradleTestKit())
}

tasks.test {
    useJUnitPlatform()
    // Monorepo root (parent of this included build) for TestKit includeBuild + dependencySubstitution.
    systemProperty("ws.engineRoot", rootDir.resolve("..").canonicalFile.absolutePath)
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
        create("web") {
            id = "io.github.worldsmiths.web"
            implementationClass = "io.github.worldsmiths.gradle.WebPlugin"
        }
        create("android") {
            id = "io.github.worldsmiths.android"
            implementationClass = "io.github.worldsmiths.gradle.AndroidPlugin"
        }
    }
}
