package io.github.worldsmiths.core.d3;

/** Immutable 3-vector for D3 pick-ray math. No LibGDX types. */
public final class Vec3 {
    public final float x;
    public final float y;
    public final float z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vec3)) {
            return false;
        }
        Vec3 other = (Vec3) obj;
        return Float.compare(other.x, x) == 0
                && Float.compare(other.y, y) == 0
                && Float.compare(other.z, z) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        result = 31 * result + Float.floatToIntBits(z);
        return result;
    }

    @Override
    public String toString() {
        return "Vec3(" + x + ", " + y + ", " + z + ")";
    }
}
