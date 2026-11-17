package io.github.worldsmiths.core;

/**
 * Built-in 3D transform component: position, euler rotation, uniform scale, and optional tint /
 * visibility in scene units.
 */
@WsComponent
public final class Transform3D {

    public float x;
    public float y;
    public float z;
    public float rotationX;
    public float rotationY;
    public float rotationZ;
    public float scale = 1f;
    public float r = 1f;
    public float g = 1f;
    public float b = 1f;
    public float a = 1f;
    public boolean visible = true;

    public Transform3D() {}

    public Transform3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
