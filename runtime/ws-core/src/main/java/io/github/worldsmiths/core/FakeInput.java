package io.github.worldsmiths.core;

import java.util.EnumSet;

/** Mutable {@link Input} for headless tests. Starts with no keys down and pointer at origin. */
public final class FakeInput implements Input {
    private final EnumSet<Key> down = EnumSet.noneOf(Key.class);
    private boolean touched;
    private boolean justTouched;
    private int x;
    private int y;

    @Override
    public boolean isKeyDown(Key key) {
        return key != null && down.contains(key);
    }

    @Override
    public boolean isTouched() {
        return touched;
    }

    @Override
    public boolean justTouched() {
        return justTouched;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public void setKeyDown(Key key, boolean isDown) {
        if (key == null) {
            return;
        }
        if (isDown) {
            down.add(key);
        } else {
            down.remove(key);
        }
    }

    public void setTouched(boolean touched) {
        this.touched = touched;
    }

    public void setJustTouched(boolean justTouched) {
        this.justTouched = justTouched;
    }

    public void setPointer(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
