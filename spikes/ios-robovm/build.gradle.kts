val javaRelease = providers.gradleProperty("javaRelease").get().toInt()

subprojects {
    apply(plugin = "java")

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(javaRelease)
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:-options")
    }
}
