package io.github.worldsmiths.desktop;

/** Window settings for the LWJGL3 desktop backend. */
public final class DesktopConfig {

    public static final String DEFAULT_TITLE = "Worldsmiths";
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final boolean DEFAULT_VSYNC = true;
    public static final boolean DEFAULT_RESIZABLE = true;

    private final String title;
    private final int width;
    private final int height;
    private final boolean vsync;
    private final boolean resizable;
    /** Optional window icon path relative to {@code assetsDir} (desktop run working directory). */
    private final String icon;

    public DesktopConfig() {
        this(DEFAULT_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_VSYNC, DEFAULT_RESIZABLE, null);
    }

    public DesktopConfig(String title, int width, int height) {
        this(title, width, height, DEFAULT_VSYNC, DEFAULT_RESIZABLE, null);
    }

    /**
     * @param icon optional path relative to {@code assetsDir} (the desktop run working directory /
     *     shipped assets root). {@code null} means no custom icon.
     */
    public DesktopConfig(
            String title, int width, int height, boolean vsync, boolean resizable, String icon) {
        if (title == null) {
            throw new NullPointerException("title");
        }
        this.title = title;
        this.width = width;
        this.height = height;
        this.vsync = vsync;
        this.resizable = resizable;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isVsync() {
        return vsync;
    }

    public boolean isResizable() {
        return resizable;
    }

    /** Window icon path relative to {@code assetsDir}, or {@code null} if unset. */
    public String getIcon() {
        return icon;
    }
}
