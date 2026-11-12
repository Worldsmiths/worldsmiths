val gdxVersion = providers.gradleProperty("gdxVersion").get()
val probe = providers.gradleProperty("probe").get()

require(probe in setOf("none", "java8api", "java11", "java17", "java17norecords")) { "Unknown probe '$probe'" }

sourceSets.named("main") {
    java.srcDir("src/probe-$probe/java")
}

dependencies {
    "implementation"("com.badlogicgames.gdx:gdx:$gdxVersion")
}
