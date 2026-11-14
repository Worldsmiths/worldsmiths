package io.github.worldsmiths.spike.game;

import io.github.worldsmiths.spike.runtime.WsComponent;

@WsComponent
public final class Transform2D {
    public float x;
    public float y;
    public float rotation;
    public float scaleX = 1f;
    public float scaleY = 1f;

    public Transform2D() {
    }

    public Transform2D(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
