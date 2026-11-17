package io.github.worldsmiths.core;

/** Mutable {@link Clock} for headless tests. Starts at zero delta and total. */
public final class FakeClock implements Clock {
    private float delta;
    private float total;

    @Override
    public float delta() {
        return delta;
    }

    @Override
    public float total() {
        return total;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    public void setTotal(float total) {
        this.total = total;
    }
}
