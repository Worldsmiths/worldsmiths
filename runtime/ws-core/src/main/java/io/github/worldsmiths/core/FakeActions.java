package io.github.worldsmiths.core;

/**
 * Mutable {@link Actions} for headless tests. Device setters write the shared {@link DeviceState}
 * that bindings poll; {@link #tick()} advances justPressed edges.
 */
public final class FakeActions implements Actions {
    private final DefaultActions delegate;

    public FakeActions(DeviceState devices) {
        this.delegate = new DefaultActions(devices);
    }

    FakeActions(DefaultActions delegate) {
        this.delegate = delegate;
    }

    DeviceState devices() {
        return delegate.devices();
    }

    @Override
    public void bind(String action, Control... controls) {
        delegate.bind(action, controls);
    }

    @Override
    public void load(Files files, String path) {
        delegate.load(files, path);
    }

    @Override
    public boolean isDown(String action) {
        return delegate.isDown(action);
    }

    @Override
    public boolean justPressed(String action) {
        return delegate.justPressed(action);
    }

    @Override
    public void tick() {
        delegate.tick();
    }

    public void setKeyDown(Key key, boolean down) {
        delegate.devices().setKeyDown(key, down);
    }

    public void setPadButtonDown(PadButton button, boolean down) {
        delegate.devices().setPadButtonDown(button, down);
    }

    public void setPointerButtonDown(PointerButton button, boolean down) {
        delegate.devices().setPointerButtonDown(button, down);
    }

    /**
     * Fake-only shortcut for logic tests that need an action without actuating a bound control.
     * Dual-device and remap proofs MUST use the real Control device path ({@link #setKeyDown}, {@link
     * #setPadButtonDown}, {@link #setPointerButtonDown}), not only this method.
     */
    public void setActionActive(String action, boolean active) {
        delegate.setActionActive(action, active);
    }
}
