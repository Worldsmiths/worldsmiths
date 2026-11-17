package io.github.worldsmiths.gradle;

/**
 * Maven module ids ({@code group:name}) and versioned notations the plugins declare.
 * External consumers resolve these via bare {@code includeBuild} (see
 * {@code docs/includebuild-substitutions.md}). Do not use {@code archivesName}.
 */
public final class EngineModuleCoordinates {

    public static final String GROUP = "io.github.worldsmiths";
    public static final String VERSION = "0.0.1-SNAPSHOT";

    /** Module id {@code group:name} — no version. */
    public static final String WS_CORE = GROUP + ":ws-core";
    public static final String BACKENDS_DESKTOP = GROUP + ":backends-desktop";
    public static final String BACKENDS_WEB = GROUP + ":backends-web";
    public static final String BACKENDS_ANDROID = GROUP + ":backends-android";
    public static final String BACKENDS_IOS = GROUP + ":backends-ios";

    /** Full Maven notation {@code group:name:version}. */
    public static final String WS_CORE_NOTATION = WS_CORE + ":" + VERSION;
    public static final String BACKENDS_DESKTOP_NOTATION = BACKENDS_DESKTOP + ":" + VERSION;
    public static final String BACKENDS_WEB_NOTATION = BACKENDS_WEB + ":" + VERSION;
    public static final String BACKENDS_ANDROID_NOTATION = BACKENDS_ANDROID + ":" + VERSION;
    public static final String BACKENDS_IOS_NOTATION = BACKENDS_IOS + ":" + VERSION;

    private EngineModuleCoordinates() {}
}
