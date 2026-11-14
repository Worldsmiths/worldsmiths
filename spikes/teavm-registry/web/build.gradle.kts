plugins {
    alias(libs.plugins.gdx.teavm)
}

// TeaVM platform launcher: gdx-teavm's backend jars are Java 17 bytecode, so this module cannot use --release 8.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

dependencies {
    implementation(project(":game"))
}

gdxTeaVM {
    // No reflection metadata at all: neither gdx-teavm's libGDX defaults nor any game patterns.
    reflectionEnabled.set(false)
    reflectionDefaults.set(false)

    webDefaults {
        mainClass.set("io.github.worldsmiths.spike.web.WebLauncher")
        htmlTitle.set("Worldsmiths S2")
        obfuscated.set(false)
    }

    js {}
    wasm {}
}
