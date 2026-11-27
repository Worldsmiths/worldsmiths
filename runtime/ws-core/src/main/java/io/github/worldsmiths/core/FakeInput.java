package io.github.worldsmiths.core;

/** Mutable {@link Input} for headless tests. Starts with no keys down and pointer at origin. */
public final class FakeInput implements Input {
    private final DeviceState devices;
    private boolean touched;
    private boolean justTouched;
    private int x;
    private int y;

    public FakeInput() {
        this(new DeviceState());
    }

    public FakeInput(DeviceState devices) {
        if (devices == null) {
            throw new EngineException("DeviceState must not be null");
        }
        this.devices = devices;
    }

    @Override
    public boolean isKeyDown(Key key) {
        return devices.isKeyDown(key);
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
        devices.setKeyDown(key, isDown);
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
