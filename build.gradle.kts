// The plugin is an included build, so a plain task name match does not run its tests.
tasks.register("build") {
    dependsOn(gradle.includedBuild("gradle-plugin").task(":check"))
    dependsOn(":runtime:ws-core:build", ":backends:desktop:build", ":bom:build")
}
