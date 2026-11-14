package io.github.worldsmiths.desktop;

/**
 * One filled-rect draw issued while walking a scene for shape rendering. Package-visible so unit
 * tests can assert traversal and cell→pixel mapping without starting LWJGL.
 */
final class ShapeDrawCommand {

    final float x;
    final float y;
    final float width;
    final float height;
    final float r;
    final float g;
    final float b;
    final float a;

    ShapeDrawCommand(
            float x, float y, float width, float height, float r, float g, float b, float a) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}
