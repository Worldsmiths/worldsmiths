package io.github.worldsmiths.gradle;

/**
 * Kotlin DSL {@code dependencySubstitution} block shared by TestKit fixtures.
 * Keep in sync with {@code docs/includebuild-substitutions.md} and sample settings.
 */
public final class IncludeBuildSubstitutions {

    private IncludeBuildSubstitutions() {}

    /** Body of {@code dependencySubstitution { ... }} for Kotlin DSL settings scripts. */
    public static String dependencySubstitutionKotlin() {
        return "    dependencySubstitution {\n"
                + "        substitute(module(\"" + EngineModuleCoordinates.BACKENDS_DESKTOP + "\"))"
                + ".using(project(\":backends:desktop\"))\n"
                + "        substitute(module(\"" + EngineModuleCoordinates.BACKENDS_WEB + "\"))"
                + ".using(project(\":backends:web\"))\n"
                + "        substitute(module(\"" + EngineModuleCoordinates.WS_CORE + "\"))"
                + ".using(project(\":runtime:ws-core\"))\n"
                + "    }\n";
    }
}
