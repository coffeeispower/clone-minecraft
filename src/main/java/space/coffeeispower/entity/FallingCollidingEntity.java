package space.coffeeispower.entity;

import org.joml.Vector3d;
import space.coffeeispower.math.AABBd;
import space.coffeeispower.world.World;

import java.text.NumberFormat;

public abstract class FallingCollidingEntity<T extends FallingCollidingEntity<T>> extends Entity<T> {
    private final float gravityAcceleration;
    private final Vector3d motion = new Vector3d();
    private boolean isOnGround = false;

    // parâmetros físicos
    private static final double airFriction = 0.98;       // resistência do ar
    private static final double groundFriction = 0.7;     // fricção com o chão

    protected FallingCollidingEntity(Vector3d position, Vector3d boundingBoxSize, float gravityAcceleration, World world) {
        super(position, boundingBoxSize, world);
        this.gravityAcceleration = gravityAcceleration;
    }

    protected FallingCollidingEntity(Vector3d position, Vector3d boundingBoxSize, World world) {
        this(position, boundingBoxSize, 22, world);
    }

    @Override
    public void update(double deltaTime) {
        System.out.println(getCurrentChunk().getPosition().toString(NumberFormat.getNumberInstance()));
        // aplicar gravidade
        motion.y -= gravityAcceleration * deltaTime;

        motion.mul(airFriction);
        isOnGround = false;

        Vector3d moveVec = new Vector3d(motion).mul(deltaTime);

        resolvePenetration();

        moveAndCollide(moveVec);

        // aplicar fricção do chão (XZ)
        if (isOnGround) {
            motion.x *= groundFriction;
            motion.z *= groundFriction;
        }

    }

    private void moveAndCollide(Vector3d moveVec) {
        AABBd box = getAABB();

        // mover eixo X
        if (moveVec.x != 0.0) {
            double allowedX = moveAxis(box, moveVec.x, 0.0, 0.0);
            position.x += allowedX;
            box = box.offset(new Vector3d(allowedX, 0, 0));
            if (allowedX != moveVec.x) motion.x = 0.0;
        }

        // mover eixo Y
        if (moveVec.y != 0.0) {
            double allowedY = moveAxis(box, 0.0, moveVec.y, 0.0);
            position.y += allowedY;
            box = box.offset(new Vector3d(0, allowedY, 0));
            if (allowedY != moveVec.y) {
                if (moveVec.y < 0) isOnGround = true;
                motion.y = 0.0;
            }
        }

        // mover eixo Z
        if (moveVec.z != 0.0) {
            double allowedZ = moveAxis(box, 0.0, 0.0, moveVec.z);
            position.z += allowedZ;
            if (allowedZ != moveVec.z) motion.z = 0.0;
        }
    }

    /**
     * Calcula o quanto é possível mover ao longo de um eixo
     */
    private double moveAxis(AABBd box, double dx, double dy, double dz) {
        double move = dx + dy + dz;
        AABBd moved = box.offset(new Vector3d(dx, dy, dz));

        // determinar limites
        int minX = (int)Math.floor(Math.min(box.min.x, moved.min.x) - 0.5);
        int maxX = (int)Math.floor(Math.max(box.max.x, moved.max.x) + 0.5);
        int minY = (int)Math.floor(Math.min(box.min.y, moved.min.y) - 0.5);
        int maxY = (int)Math.floor(Math.max(box.max.y, moved.max.y) + 0.5);
        int minZ = (int)Math.floor(Math.min(box.min.z, moved.min.z) - 0.5);
        int maxZ = (int)Math.floor(Math.max(box.max.z, moved.max.z) + 0.5);

        double allowed = move;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!world.isSolid(x, y, z)) continue;

                    // agora o bloco está centrado
                    AABBd block = new AABBd(
                            new Vector3d(x - 0.5, y - 0.5, z - 0.5),
                            new Vector3d(x + 0.5, y + 0.5, z + 0.5)
                    );

                    if (!moved.intersects(block)) continue;

                    if (dx > 0.0) {
                        double candidate = block.min.x - box.max.x;
                        if (candidate < allowed) allowed = Math.min(allowed, candidate);
                    } else if (dx < 0.0) {
                        double candidate = block.max.x - box.min.x;
                        if (candidate > allowed) allowed = Math.max(allowed, candidate);
                    } else if (dy > 0.0) {
                        double candidate = block.min.y - box.max.y;
                        if (candidate < allowed) allowed = Math.min(allowed, candidate);
                    } else if (dy < 0.0) {
                        double candidate = block.max.y - box.min.y;
                        if (candidate > allowed) allowed = Math.max(allowed, candidate);
                    } else if (dz > 0.0) {
                        double candidate = block.min.z - box.max.z;
                        if (candidate < allowed) allowed = Math.min(allowed, candidate);
                    } else if (dz < 0.0) {
                        double candidate = block.max.z - box.min.z;
                        if (candidate > allowed) allowed = Math.max(allowed, candidate);
                    }
                }
            }
        }

        return Math.abs(allowed) < 1e-6 ? 0.0 : allowed;
    }

    /** empurra para fora se estiver dentro de blocos */
    private void resolvePenetration() {
        for (int attempt = 0; attempt < 3; attempt++) {
            AABBd box = getAABB();
            boolean collided = false;

            int minX = (int)Math.floor(box.min.x - 0.5);
            int maxX = (int)Math.floor(box.max.x + 0.5);
            int minY = (int)Math.floor(box.min.y - 0.5);
            int maxY = (int)Math.floor(box.max.y + 0.5);
            int minZ = (int)Math.floor(box.min.z - 0.5);
            int maxZ = (int)Math.floor(box.max.z + 0.5);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!world.isSolid(x, y, z)) continue;
                        AABBd block = new AABBd(
                                new Vector3d(x - 0.5, y - 0.5, z - 0.5),
                                new Vector3d(x + 0.5, y + 0.5, z + 0.5)
                        );
                        if (!box.intersects(block)) continue;

                        collided = true;

                        double ox = Math.min(box.max.x, block.max.x) - Math.max(box.min.x, block.min.x);
                        double oy = Math.min(box.max.y, block.max.y) - Math.max(box.min.y, block.min.y);
                        double oz = Math.min(box.max.z, block.max.z) - Math.max(box.min.z, block.min.z);

                        // escolhe menor eixo de penetração
                        double minOverlap = Math.min(ox, Math.min(oy, oz));

                        if (minOverlap == ox) {
                            double dir = (position.x < block.min.x) ? -1 : 1;
                            position.x += dir * ox;
                        } else if (minOverlap == oy) {
                            double dir = (position.y < block.min.y) ? -1 : 1;
                            position.y += dir * oy;
                        } else {
                            double dir = (position.z < block.min.z) ? -1 : 1;
                            position.z += dir * oz;
                        }
                    }
                }
            }

            if (!collided) break;
        }
    }

    public AABBd getAABB() {
        Vector3d half = new Vector3d(boundingBoxSize).mul(0.5);
        return new AABBd(
                new Vector3d(position).sub(half),
                new Vector3d(position).add(half)
        );
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public Vector3d getMotion() {
        return motion;
    }

}
