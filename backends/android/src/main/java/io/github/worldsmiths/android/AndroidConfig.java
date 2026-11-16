package io.github.worldsmiths.android;

/**
 * Curated Android launch settings mapped onto libGDX {@code AndroidApplicationConfiguration}
 * fields by the generated Activity (AAR is not a compile dependency of this JVM module).
 *
 * <p>Defaults match libGDX 1.14.2 {@code AndroidApplicationConfiguration} field defaults
 * (immersive / sensors / wakelock). Width/height mirror desktop/web DSL curated size; Android
 * has no windowed-mode fields on {@code AndroidApplicationConfiguration}, so the host keeps them
 * on this config for attach parity (surface size remains device/fill-strategy driven).
 */
public final class AndroidConfig {

    /** Same default as desktop/web DSL ({@code WorldsmithsSettingsPlugin.DEFAULT_WIDTH}). */
    public static final int DEFAULT_WIDTH = 800;
    /** Same default as desktop/web DSL ({@code WorldsmithsSettingsPlugin.DEFAULT_HEIGHT}). */
    public static final int DEFAULT_HEIGHT = 600;

    public static final boolean DEFAULT_USE_IMMERSIVE_MODE = true;
    public static final boolean DEFAULT_USE_ACCELEROMETER = true;
    public static final boolean DEFAULT_USE_GYROSCOPE = false;
    public static final boolean DEFAULT_USE_COMPASS = true;
    public static final boolean DEFAULT_USE_WAKELOCK = false;

    private final int width;
    private final int height;
    private final boolean useImmersiveMode;
    private final boolean useAccelerometer;
    private final boolean useGyroscope;
    private final boolean useCompass;
    private final boolean useWakelock;

    public AndroidConfig() {
        this(
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                DEFAULT_USE_IMMERSIVE_MODE,
                DEFAULT_USE_ACCELEROMETER,
                DEFAULT_USE_GYROSCOPE,
                DEFAULT_USE_COMPASS,
                DEFAULT_USE_WAKELOCK);
    }

    public AndroidConfig(int width, int height) {
        this(
                width,
                height,
                DEFAULT_USE_IMMERSIVE_MODE,
                DEFAULT_USE_ACCELEROMETER,
                DEFAULT_USE_GYROSCOPE,
                DEFAULT_USE_COMPASS,
                DEFAULT_USE_WAKELOCK);
    }

    public AndroidConfig(
            boolean useImmersiveMode,
            boolean useAccelerometer,
            boolean useGyroscope,
            boolean useCompass,
            boolean useWakelock) {
        this(
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                useImmersiveMode,
                useAccelerometer,
                useGyroscope,
                useCompass,
                useWakelock);
    }

    public AndroidConfig(
            int width,
            int height,
            boolean useImmersiveMode,
            boolean useAccelerometer,
            boolean useGyroscope,
            boolean useCompass,
            boolean useWakelock) {
        this.width = width;
        this.height = height;
        this.useImmersiveMode = useImmersiveMode;
        this.useAccelerometer = useAccelerometer;
        this.useGyroscope = useGyroscope;
        this.useCompass = useCompass;
        this.useWakelock = useWakelock;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isUseImmersiveMode() {
        return useImmersiveMode;
    }

    public boolean isUseAccelerometer() {
        return useAccelerometer;
    }

    public boolean isUseGyroscope() {
        return useGyroscope;
    }

    public boolean isUseCompass() {
        return useCompass;
    }

    public boolean isUseWakelock() {
        return useWakelock;
    }
}
