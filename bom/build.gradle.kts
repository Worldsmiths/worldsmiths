plugins {
    `java-platform`
}

group = libs.versions.group.get()
version = libs.versions.engine.get()

dependencies {
    constraints {
        api(project(":runtime:ws-core"))
    }
}
