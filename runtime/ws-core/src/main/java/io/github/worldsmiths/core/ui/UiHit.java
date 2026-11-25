package io.github.worldsmiths.core.ui;

/** Result of a logical top-left hit test. */
public final class UiHit {
    private static final UiHit NONE = new UiHit(null, false);

    private final String id;
    private final boolean activated;

    public UiHit(String id, boolean activated) {
        this.id = id;
        this.activated = activated;
    }

    public static UiHit none() {
        return NONE;
    }

    public String id() {
        return id;
    }

    public boolean activated() {
        return activated;
    }
}