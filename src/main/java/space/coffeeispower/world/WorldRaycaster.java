package space.coffeeispower.world;

import org.joml.Intersectiond;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3i;
import space.coffeeispower.math.AABBd;

public class WorldRaycaster {


    /** Faz um raycast e vê qual o bloco que o raio está a apontar */
    public static BlockRaycastResult findClosestBlock(Vector3d origin, Vector3d direction, World world, double maxDistance) {
        direction = direction.normalize(new Vector3d());
        Vector3d start = new Vector3d(origin);
        double closestDistance = maxDistance;
        Vector3d hitPoint = null;
        Vector3i hitBlock = null;
        AABBd aabb = new AABBd();
        // limites aproximados para iterar
        int minX = (int)Math.floor(start.x - maxDistance);
        int maxX = (int)Math.ceil(start.x + maxDistance);
        int minY = (int)Math.floor(start.y - maxDistance);
        int maxY = (int)Math.ceil(start.y + maxDistance);
        int minZ = (int)Math.floor(start.z - maxDistance);
        int maxZ = (int)Math.ceil(start.z + maxDistance);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!world.isSolid(x, y, z)) continue;

                    Vector3d min = new Vector3d(x - 0.5, y - 0.5, z - 0.5);
                    Vector3d max = new Vector3d(x + 0.5, y + 0.5, z + 0.5);

                    Vector2d distanceNearFar = new Vector2d();
                    if (Intersectiond.intersectRayAab(origin, direction, min, max, distanceNearFar)) {
                        var distance = distanceNearFar.x;
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            hitPoint = direction.mul(distance, new Vector3d());
                            hitBlock = new Vector3i(x, y, z);
                            aabb.max.set(max);
                            aabb.min.set(min);
                        }
                    }
                }
            }
        }

        if (hitBlock != null) {
            return new BlockRaycastResult(hitBlock, hitPoint, aabb);
        }
        return null;
    }

    public record BlockRaycastResult(Vector3i blockPosition, Vector3d intersectionPoint, AABBd hitbox) {
        public enum BlockFace {
            TOP, BOTTOM, NORTH, SOUTH, WEST, EAST
        }

        public BlockFace getHitFace() {
            final double eps = 1e-6; // tolerância para erros de ponto flutuante
            AABBd box = this.hitbox();
            Vector3d p = this.intersectionPoint();

            if (Math.abs(p.x - box.min.x) < eps) return BlockFace.WEST;
            if (Math.abs(p.x - box.max.x) < eps) return BlockFace.EAST;

            if (Math.abs(p.y - box.min.y) < eps) return BlockFace.BOTTOM;
            if (Math.abs(p.y - box.max.y) < eps) return BlockFace.TOP;

            if (Math.abs(p.z - box.min.z) < eps) return BlockFace.NORTH;
            if (Math.abs(p.z - box.max.z) < eps) return BlockFace.SOUTH;

            // fallback, nunca deveria chegar aqui
            return null;
        }
    }
}
