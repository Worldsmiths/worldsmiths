package io.github.worldsmiths.gradle;

/**
 * Android toolchain pins consumed by the future {@code AndroidPlugin} (D6).
 * Keep in sync with {@code gradle/libs.versions.toml} and the AGP classpath
 * dependency in {@code gradle-plugin/build.gradle.kts} (TeaVM-style).
 */
public final class AndroidSdkPins {

    /** AGP on the gradle-plugin classpath; games do not declare AGP. */
    public static final String AGP_VERSION = "8.13.2";

    public static final int MIN_SDK = 21;
    public static final int COMPILE_SDK = 36;
    public static final int TARGET_SDK = 36;

    private AndroidSdkPins() {}
}
