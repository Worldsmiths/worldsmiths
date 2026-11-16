package io.github.worldsmiths.android;

/**
 * Curated Android launch settings mapped onto libGDX {@code AndroidApplicationConfiguration}
 * fields by the generated Activity (AAR is not a compile dependency of this JVM module).
 *
 * <p>Defaults match libGDX 1.14.2 {@code AndroidApplicationConfiguration} field defaults
 * (immersive / sensors / wakelock).
 */
public final class AndroidConfig {

    public static final boolean DEFAULT_USE_IMMERSIVE_MODE = true;
    public static final boolean DEFAULT_USE_ACCELEROMETER = true;
    public static final boolean DEFAULT_USE_GYROSCOPE = false;
    public static final boolean DEFAULT_USE_COMPASS = true;
    public static final boolean DEFAULT_USE_WAKELOCK = false;

    private final boolean useImmersiveMode;
    private final boolean useAccelerometer;
    private final boolean useGyroscope;
    private final boolean useCompass;
    private final boolean useWakelock;

    public AndroidConfig() {
        this(
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
        this.useImmersiveMode = useImmersiveMode;
        this.useAccelerometer = useAccelerometer;
        this.useGyroscope = useGyroscope;
        this.useCompass = useCompass;
        this.useWakelock = useWakelock;
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
