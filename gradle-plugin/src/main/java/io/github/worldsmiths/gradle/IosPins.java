package io.github.worldsmiths.gradle;

/**
 * iOS toolchain pins consumed by the future {@code IosPlugin} (D7).
 * Keep in sync with {@code gradle/libs.versions.toml} and the RoboVM classpath
 * dependency in {@code gradle-plugin/build.gradle.kts} (TeaVM/AGP-style).
 */
public final class IosPins {

    /**
     * RoboVM / MobiVM on the gradle-plugin classpath; games do not declare RoboVM.
     * Floor is ≥2.3.26 for configuration-cache must-not-fail; tip pin is 2.3.27.
     */
    public static final String ROBOVM_VERSION = "2.3.27";

    /** libGDX version; must match catalog {@code gdx}. */
    public static final String GDX_VERSION = "1.14.2";

    public static final String GDX_BACKEND_ROBOVM_METALANGLE =
            "com.badlogicgames.gdx:gdx-backend-robovm-metalangle:" + GDX_VERSION;

    public static final String GDX_PLATFORM =
            "com.badlogicgames.gdx:gdx-platform:" + GDX_VERSION;

    /** Classifier on {@link #GDX_PLATFORM} for iOS natives (same pattern as desktop/android). */
    public static final String NATIVES_IOS_CLASSIFIER = "natives-ios";

    public static final String ROBOVM_GRADLE_PLUGIN =
            "com.mobidevelop.robovm:robovm-gradle-plugin:" + ROBOVM_VERSION;

    private IosPins() {}
}
