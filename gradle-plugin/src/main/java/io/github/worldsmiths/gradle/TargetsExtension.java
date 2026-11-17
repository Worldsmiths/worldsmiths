package io.github.worldsmiths.gradle;

import org.gradle.api.Action;
import org.gradle.api.tasks.Nested;

/** The {@code targets {}} block in {@code settings.gradle.kts}. */
public abstract class TargetsExtension {

    private boolean webConfigured;
    private boolean androidConfigured;
    private boolean iosConfigured;

    @Nested
    public abstract DesktopTargetExtension getDesktop();

    @Nested
    public abstract WebTargetExtension getWeb();

    @Nested
    public abstract AndroidTargetExtension getAndroid();

    @Nested
    public abstract IosTargetExtension getIos();

    public void desktop(Action<? super DesktopTargetExtension> action) {
        action.execute(getDesktop());
    }

    /**
     * Opt-in web target. Calling this block (even empty) causes the settings plugin to generate
     * {@code :web}; desktop-only samples stay unchanged until they opt in.
     */
    public void web(Action<? super WebTargetExtension> action) {
        webConfigured = true;
        action.execute(getWeb());
    }

    /**
     * Opt-in Android target. Calling this block (even empty) causes the settings plugin to generate
     * {@code :android}; samples stay unchanged until they opt in.
     */
    public void android(Action<? super AndroidTargetExtension> action) {
        androidConfigured = true;
        action.execute(getAndroid());
    }

    /**
     * Opt-in iOS target. Calling this block (even empty) causes the settings plugin to generate
     * {@code :ios}; samples stay unchanged until they opt in.
     */
    public void ios(Action<? super IosTargetExtension> action) {
        iosConfigured = true;
        action.execute(getIos());
    }

    boolean isWebConfigured() {
        return webConfigured;
    }

    boolean isAndroidConfigured() {
        return androidConfigured;
    }

    boolean isIosConfigured() {
        return iosConfigured;
    }
}
