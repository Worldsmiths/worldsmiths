package io.github.worldsmiths.gradle;

/**
 * Kotlin DSL {@code includeBuild} helper for TestKit fixtures.
 * Happy path is bare {@code includeBuild} — Gradle auto-substitutes when project
 * names match Maven artifact ids (see {@code docs/includebuild-substitutions.md}).
 */
public final class IncludeBuildSubstitutions {

    private IncludeBuildSubstitutions() {}

    /**
     * Bare {@code includeBuild(path)} line for Kotlin DSL settings scripts (no
     * {@code dependencySubstitution} block).
     */
    public static String bareIncludeBuildKotlin(String engineRootPath) {
        return "includeBuild(\"" + engineRootPath + "\")\n";
    }
}
