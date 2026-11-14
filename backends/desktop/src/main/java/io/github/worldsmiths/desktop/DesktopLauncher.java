package io.github.worldsmiths.desktop;

import io.github.worldsmiths.core.EngineInfo;

/**
 * Thin facade retained for module smoke identity. Prefer {@link DesktopBackend#start} from
 * generated launchers (T3).
 */
public final class DesktopLauncher {
    private DesktopLauncher() {}

    public static String label() {
        return EngineInfo.group();
    }
}
