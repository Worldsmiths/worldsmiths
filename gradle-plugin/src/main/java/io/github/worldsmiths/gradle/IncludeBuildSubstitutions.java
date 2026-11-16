package io.github.worldsmiths.gradle;

/**
 * Kotlin DSL {@code dependencySubstitution} block shared by TestKit fixtures.
 * Keep in sync with {@code docs/includebuild-substitutions.md} and sample settings.
 */
public final class IncludeBuildSubstitutions {

    private IncludeBuildSubstitutions() {}

    /**
     * Kotlin DSL substitute line for {@code backends-android}.
     * Not yet folded into {@link #dependencySubstitutionKotlin()} — Gradle rejects
     * {@code project(":backends:android")} until that module is included (D6 T2).
     */
    public static String backendsAndroidSubstituteKotlin() {
        return "        substitute(module(\"" + EngineModuleCoordinates.BACKENDS_ANDROID + "\"))"
                + ".using(project(\":backends:android\"))\n";
    }

    /**
     * Body of {@code dependencySubstitution { ... }} for Kotlin DSL settings scripts.
     * Desktop / web / ws-core only until {@code :backends:android} exists.
     */
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
