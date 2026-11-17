package io.github.worldsmiths.ios;

import com.badlogic.gdx.ApplicationListener;

/**
 * Stand-in for libGDX {@code IOSApplication} construction so this module stays a plain JVM
 * {@code java-library}. The generated Delegate (with MetalANGLE / iosrobovm) implements this by
 * mapping {@link IosConfig} onto {@code IOSApplicationConfiguration} and constructing {@code
 * IOSApplication}.
 */
@FunctionalInterface
public interface IosApplicationHost {

    /**
     * Initialize the iOS application with the given listener and Worldsmiths config.
     *
     * @param listener game listener to drive
     * @param config curated settings to map onto libGDX iOS configuration
     */
    void initialize(ApplicationListener listener, IosConfig config);
}
