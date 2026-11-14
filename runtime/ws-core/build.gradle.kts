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
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

abstract class GenerateGroupFile : DefaultTask() {
    @get:Input
    abstract val groupId: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("worldsmiths-group.txt").writeText(groupId.get())
    }
}

val generateGroupId = tasks.register<GenerateGroupFile>("generateGroupId") {
    groupId.set(libs.versions.group)
    outputDir.set(layout.buildDirectory.dir("generated/resources"))
}

sourceSets.named("main") {
    resources.srcDir(generateGroupId.flatMap { it.outputDir })
}

tasks.named("processResources") {
    dependsOn(generateGroupId)
}
