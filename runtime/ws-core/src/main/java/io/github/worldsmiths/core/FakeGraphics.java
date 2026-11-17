package io.github.worldsmiths.core;

/** Mutable {@link Graphics} for headless tests. Records the last {@link #setTitle(String)}. */
public final class FakeGraphics implements Graphics {
    private int width;
    private int height;
    private String lastTitle;

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setTitle(String title) {
        this.lastTitle = title;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /** Most recent title passed to {@link #setTitle(String)}, or {@code null} if never set. */
    public String lastTitle() {
        return lastTitle;
    }
}
