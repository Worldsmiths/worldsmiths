package io.github.worldsmiths.android;

import com.badlogic.gdx.ApplicationListener;

/**
 * Stand-in for libGDX {@code AndroidApplication#initialize} so this module stays a plain JVM
 * {@code java-library}. The generated Activity (with {@code gdx-backend-android} AAR) implements
 * this by mapping {@link AndroidConfig} onto {@code AndroidApplicationConfiguration} and calling
 * {@code initialize(listener, gdxConfig)}.
 */
@FunctionalInterface
public interface AndroidApplicationHost {

    /**
     * Initialize the Android application with the given listener and Worldsmiths config.
     *
     * @param listener game listener to drive
     * @param config curated settings to map onto libGDX Android configuration
     */
    void initialize(ApplicationListener listener, AndroidConfig config);
}
