package io.github.worldsmiths.core;

/**
 * Built-in sprite component. {@code texture} is a forward-compatible asset key; D2 draws with
 * {@code r}/{@code g}/{@code b}/{@code a} shape colors instead of loading the texture.
 */
public final class Sprite {

    public String texture;
    public float r = 1f;
    public float g = 1f;
    public float b = 1f;
    public float a = 1f;
    public int layer;
    public boolean visible = true;

    public Sprite() {}

    public Sprite(String texture) {
        this.texture = texture;
    }

    public Sprite(String texture, int layer) {
        this.texture = texture;
        this.layer = layer;
    }
}
