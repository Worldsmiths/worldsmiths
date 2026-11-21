package io.github.worldsmiths.core.thin3d;

/** Immutable pick ray: origin + direction (not necessarily unit). No LibGDX types. */
public final class Ray {
    public final Vec3 origin;
    public final Vec3 direction;

    public Ray(Vec3 origin, Vec3 direction) {
        if (origin == null || direction == null) {
            throw new IllegalArgumentException("origin and direction must be non-null");
        }
        this.origin = origin;
        this.direction = direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ray)) {
            return false;
        }
        Ray other = (Ray) obj;
        return origin.equals(other.origin) && direction.equals(other.direction);
    }

    @Override
    public int hashCode() {
        return 31 * origin.hashCode() + direction.hashCode();
    }

    @Override
    public String toString() {
        return "Ray(origin=" + origin + ", direction=" + direction + ")";
    }
}
