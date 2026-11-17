package io.github.worldsmiths.gradle;

/**
 * Kotlin DSL {@code dependencySubstitution} block shared by TestKit fixtures.
 * Keep in sync with {@code docs/includebuild-substitutions.md} and sample settings.
 */
public final class IncludeBuildSubstitutions {

    private IncludeBuildSubstitutions() {}

    /**
     * Kotlin DSL substitute line for {@code backends-android} → {@code :backends:android}.
     */
    public static String backendsAndroidSubstituteKotlin() {
        return "        substitute(module(\"" + EngineModuleCoordinates.BACKENDS_ANDROID + "\"))"
                + ".using(project(\":backends:android\"))\n";
    }

    /**
     * Kotlin DSL substitute line for {@code backends-ios} → {@code :backends:ios}.
     */
    public static String backendsIosSubstituteKotlin() {
        return "        substitute(module(\"" + EngineModuleCoordinates.BACKENDS_IOS + "\"))"
                + ".using(project(\":backends:ios\"))\n";
    }

    /**
     * Body of {@code dependencySubstitution { ... }} for Kotlin DSL settings scripts.
     * Desktop / web / android / ios / ws-core.
     */
    public static String dependencySubstitutionKotlin() {
        return "    dependencySubstitution {\n"
                + "        substitute(module(\"" + EngineModuleCoordinates.BACKENDS_DESKTOP + "\"))"
                + ".using(project(\":backends:desktop\"))\n"
                + "        substitute(module(\"" + EngineModuleCoordinates.BACKENDS_WEB + "\"))"
                + ".using(project(\":backends:web\"))\n"
                + backendsAndroidSubstituteKotlin()
                + backendsIosSubstituteKotlin()
                + "        substitute(module(\"" + EngineModuleCoordinates.WS_CORE + "\"))"
                + ".using(project(\":runtime:ws-core\"))\n"
                + "    }\n";
    }
}
