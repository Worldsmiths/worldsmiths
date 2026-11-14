package io.github.worldsmiths.desktop;

import io.github.worldsmiths.core.EngineInfo;

public final class DesktopLauncher {
    private DesktopLauncher() {}

    public static String label() {
        return EngineInfo.group();
    }

    public static void main(String[] args) {
        // No window. The libGDX desktop backend is a later milestone.
    }
}
