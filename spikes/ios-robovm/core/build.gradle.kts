val gdxVersion: String by project
val probe = providers.gradleProperty("probe").get()

require(probe in setOf("none", "java8api", "java11")) { "Unknown probe '$probe'" }

sourceSets.named("main") {
    java.srcDir("src/probe-$probe/java")
}

dependencies {
    "implementation"("com.badlogicgames.gdx:gdx:$gdxVersion")
}
