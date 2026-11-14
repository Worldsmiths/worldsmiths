package io.github.worldsmiths.spike.game;

import io.github.worldsmiths.spike.runtime.WsComponent;

@WsComponent
public final class Sprite {
    public String texture;
    public int layer;
    public float alpha = 1f;
    public boolean visible = true;

    public Sprite() {
    }

    public Sprite(String texture, int layer) {
        this.texture = texture;
        this.layer = layer;
    }
}
