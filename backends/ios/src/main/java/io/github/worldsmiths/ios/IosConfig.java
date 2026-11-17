package io.github.worldsmiths.ios;

/**
 * Curated iOS launch settings mapped onto libGDX {@code IOSApplicationConfiguration} fields by
 * the generated Delegate (MetalANGLE / iosrobovm are not compile dependencies of this JVM module).
 *
 * <p>Defaults match libGDX 1.14.2 {@code IOSApplicationConfiguration} field defaults for the
 * curated subset (orientations / sensors / screen dimming / home indicator). Width/height mirror
 * desktop/web/android DSL curated size for attach parity (surface size remains device-driven).
 */
public final class IosConfig {

    /** Same default as desktop/web DSL ({@code WorldsmithsSettingsPlugin.DEFAULT_WIDTH}). */
    public static final int DEFAULT_WIDTH = 800;
    /** Same default as desktop/web DSL ({@code WorldsmithsSettingsPlugin.DEFAULT_HEIGHT}). */
    public static final int DEFAULT_HEIGHT = 600;

    public static final boolean DEFAULT_PREVENT_SCREEN_DIMMING = true;
    public static final boolean DEFAULT_ORIENTATION_PORTRAIT = true;
    public static final boolean DEFAULT_ORIENTATION_LANDSCAPE = true;
    public static final boolean DEFAULT_USE_ACCELEROMETER = true;
    public static final boolean DEFAULT_USE_COMPASS = true;
    public static final boolean DEFAULT_HIDE_HOME_INDICATOR = false;

    private final int width;
    private final int height;
    private final boolean preventScreenDimming;
    private final boolean orientationPortrait;
    private final boolean orientationLandscape;
    private final boolean useAccelerometer;
    private final boolean useCompass;
    private final boolean hideHomeIndicator;

    public IosConfig() {
        this(
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                DEFAULT_PREVENT_SCREEN_DIMMING,
                DEFAULT_ORIENTATION_PORTRAIT,
                DEFAULT_ORIENTATION_LANDSCAPE,
                DEFAULT_USE_ACCELEROMETER,
                DEFAULT_USE_COMPASS,
                DEFAULT_HIDE_HOME_INDICATOR);
    }

    public IosConfig(int width, int height) {
        this(
                width,
                height,
                DEFAULT_PREVENT_SCREEN_DIMMING,
                DEFAULT_ORIENTATION_PORTRAIT,
                DEFAULT_ORIENTATION_LANDSCAPE,
                DEFAULT_USE_ACCELEROMETER,
                DEFAULT_USE_COMPASS,
                DEFAULT_HIDE_HOME_INDICATOR);
    }

    /**
     * Curated orientation / sensor / chrome flags with default width/height.
     *
     * @param preventScreenDimming maps to {@code IOSApplicationConfiguration.preventScreenDimming}
     * @param orientationPortrait maps to {@code IOSApplicationConfiguration.orientationPortrait}
     * @param orientationLandscape maps to {@code IOSApplicationConfiguration.orientationLandscape}
     * @param useAccelerometer maps to {@code IOSApplicationConfiguration.useAccelerometer}
     * @param hideHomeIndicator maps to {@code IOSApplicationConfiguration.hideHomeIndicator}
     */
    public IosConfig(
            boolean preventScreenDimming,
            boolean orientationPortrait,
            boolean orientationLandscape,
            boolean useAccelerometer,
            boolean hideHomeIndicator) {
        this(
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                preventScreenDimming,
                orientationPortrait,
                orientationLandscape,
                useAccelerometer,
                DEFAULT_USE_COMPASS,
                hideHomeIndicator);
    }

    public IosConfig(
            int width,
            int height,
            boolean preventScreenDimming,
            boolean orientationPortrait,
            boolean orientationLandscape,
            boolean useAccelerometer,
            boolean useCompass,
            boolean hideHomeIndicator) {
        this.width = width;
        this.height = height;
        this.preventScreenDimming = preventScreenDimming;
        this.orientationPortrait = orientationPortrait;
        this.orientationLandscape = orientationLandscape;
        this.useAccelerometer = useAccelerometer;
        this.useCompass = useCompass;
        this.hideHomeIndicator = hideHomeIndicator;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isPreventScreenDimming() {
        return preventScreenDimming;
    }

    public boolean isOrientationPortrait() {
        return orientationPortrait;
    }

    public boolean isOrientationLandscape() {
        return orientationLandscape;
    }

    public boolean isUseAccelerometer() {
        return useAccelerometer;
    }

    public boolean isUseCompass() {
        return useCompass;
    }

    public boolean isHideHomeIndicator() {
        return hideHomeIndicator;
    }
}
