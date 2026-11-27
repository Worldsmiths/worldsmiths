package io.github.worldsmiths.gradle;

/**
 * Android toolchain pins consumed by {@link AndroidPlugin}.
 * Keep in sync with {@code gradle/libs.versions.toml} and the AGP classpath
 * dependency in {@code gradle-plugin/build.gradle.kts} (TeaVM-style).
 */
public final class AndroidSdkPins {

    /**
     * AGP on the gradle-plugin classpath; games do not declare AGP.
     *
     * <p>D6 interview pinned 8.13.x, but AGP 8.x cannot apply on Gradle 9.6+ (removed
     * {@code InternalProblems}). Worldsmiths is on Gradle 9.9.1, so Phase-0 uses AGP 9.0.0.
     * See {@code .agent-docs/implement-spec-d6-android-agp/changes.md}.
     */
    public static final String AGP_VERSION = "9.0.0";

    /** libGDX version; must match catalog {@code gdx}. */
    public static final String GDX_VERSION = "1.14.2";

    /**
     * gdx-controllers pin; must match catalog {@code gdx-controllers}. Spike-resolved against
     * {@link #GDX_VERSION} (AAR — wire on the generated {@code :android} app, not {@code
     * backends/android}).
     */
    public static final String GDX_CONTROLLERS_VERSION = "2.2.4";

    public static final String GDX_BACKEND_ANDROID =
            "com.badlogicgames.gdx:gdx-backend-android:" + GDX_VERSION;

    public static final String GDX_PLATFORM =
            "com.badlogicgames.gdx:gdx-platform:" + GDX_VERSION;

    public static final String GDX_CONTROLLERS_ANDROID =
            "com.badlogicgames.gdx-controllers:gdx-controllers-android:" + GDX_CONTROLLERS_VERSION;

    public static final int MIN_SDK = 21;
    public static final int COMPILE_SDK = 36;
    public static final int TARGET_SDK = 36;

    private AndroidSdkPins() {}
}
