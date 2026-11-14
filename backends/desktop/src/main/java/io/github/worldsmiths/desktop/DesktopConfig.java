package io.github.worldsmiths.desktop;

/** Window settings for the LWJGL3 desktop backend. */
public final class DesktopConfig {

    public static final String DEFAULT_TITLE = "Worldsmiths";
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;

    private final String title;
    private final int width;
    private final int height;

    public DesktopConfig() {
        this(DEFAULT_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public DesktopConfig(String title, int width, int height) {
        if (title == null) {
            throw new NullPointerException("title");
        }
        this.title = title;
        this.width = width;
        this.height = height;
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
}
