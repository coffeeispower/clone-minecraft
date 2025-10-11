package space.coffeeispower.minecraft_clone.math;

import org.joml.Vector3d;

public class AABBd {
    public final Vector3d min;
    public final Vector3d max;

    public AABBd(Vector3d min, Vector3d max) {
        this.min = min;
        this.max = max;
    }
    public AABBd() {
        this.min = new Vector3d();
        this.max = new Vector3d();
    }

    public boolean intersects(AABBd other) {
        return (min.x < other.max.x && max.x > other.min.x) &&
                (min.y < other.max.y && max.y > other.min.y) &&
                (min.z < other.max.z && max.z > other.min.z);
    }

    public AABBd offset(Vector3d v) {
        return new AABBd(new Vector3d(min).add(v), new Vector3d(max).add(v));
    }

    public AABBd offset(double x, double y, double z) {
        return new AABBd(new Vector3d(min).add(x, y, z), new Vector3d(max).add(x, y, z));
    }
}
