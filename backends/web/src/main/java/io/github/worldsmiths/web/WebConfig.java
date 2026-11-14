package io.github.worldsmiths.web;

/** Window settings for the TeaVM web backend. */
public final class WebConfig {

    public static final String DEFAULT_TITLE = "Worldsmiths";
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;

    private final String title;
    private final int width;
    private final int height;

    public WebConfig() {
        this(DEFAULT_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public WebConfig(String title, int width, int height) {
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
