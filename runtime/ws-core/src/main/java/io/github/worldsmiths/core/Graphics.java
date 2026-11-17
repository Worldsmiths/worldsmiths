package io.github.worldsmiths.core;

/**
 * Portable graphics meta: framebuffer size and a best-effort window title. {@link #setTitle(String)}
 * may be ignored on platforms without a window title (e.g. Android).
 */
public interface Graphics {

    /** Backbuffer / window width in pixels. */
    int getWidth();

    /** Backbuffer / window height in pixels. */
    int getHeight();

    /**
     * Best-effort window title update. Desktop/web may apply it; mobile may ignore. Never throws for
     * an unsupported platform.
     */
    void setTitle(String title);
}
