package io.github.worldsmiths.gradle;

import org.gradle.api.Action;
import org.gradle.api.tasks.Nested;

/** The {@code targets {}} block in {@code settings.gradle.kts}. */
public abstract class TargetsExtension {

    @Nested
    public abstract DesktopTargetExtension getDesktop();

    public void desktop(Action<? super DesktopTargetExtension> action) {
        action.execute(getDesktop());
    }
}
