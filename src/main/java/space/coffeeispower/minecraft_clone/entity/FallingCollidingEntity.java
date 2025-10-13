package space.coffeeispower.minecraft_clone.entity;

import org.joml.Vector3d;
import space.coffeeispower.minecraft_clone.math.AABBd;
import space.coffeeispower.minecraft_clone.world.World;

public abstract class FallingCollidingEntity<T extends FallingCollidingEntity<T>> extends Entity<T> {
    private final float gravityAcceleration;
    private final Vector3d motion = new Vector3d();
    private boolean isOnGround = false;

    // parâmetros físicos
    private static final double airFriction = 0.94;       // resistência do ar
    private static final double groundFriction = 0.85;     // fricção com o chão

    protected FallingCollidingEntity(Vector3d position, Vector3d boundingBoxSize, float gravityAcceleration, World world, double maxHp) {
        super(position, boundingBoxSize, world, maxHp);
        this.gravityAcceleration = gravityAcceleration;
    }

    protected FallingCollidingEntity(Vector3d position, Vector3d boundingBoxSize, World world, double maxHp) {
        this(position, boundingBoxSize, 25.6f, world, maxHp);
    }

    @Override
    public void update(double deltaTime) {
        if(getCurrentChunk() == null) return;
        // aplicar gravidade
        motion.mul(airFriction, 1, airFriction);
        motion.y -= gravityAcceleration * deltaTime;
        motion.y = Math.min(motion.y, 8);
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
        double maxStep = 0.05; // metade de um bloco por passo
        double remaining = moveVec.length();
        if (remaining == 0.0) return;

        Vector3d direction = new Vector3d(moveVec).normalize();

        while (remaining > 0.0) {
            double stepLength = Math.min(maxStep, remaining);
            Vector3d step = new Vector3d(direction).mul(stepLength);

            // mover eixo X
            if (step.x != 0.0) {
                double allowedX = moveAxis(getAABB(), step.x, 0.0, 0.0);
                position.x += allowedX;
                if (allowedX != step.x) motion.x = 0.0;
            }

            // mover eixo Y
            if (step.y != 0.0) {
                double allowedY = moveAxis(getAABB(), 0.0, step.y, 0.0);
                position.y += allowedY;
                if (allowedY != step.y) {
                    if (step.y < 0) isOnGround = true;
                    motion.y = 0.0;
                }
            }

            // mover eixo Z
            if (step.z != 0.0) {
                double allowedZ = moveAxis(getAABB(), 0.0, 0.0, step.z);
                position.z += allowedZ;
                if (allowedZ != step.z) motion.z = 0.0;
            }

            remaining -= stepLength;
        }
    }

    private static final double EPSILON = 1e-6;

    /**
     * Calcula o quanto é possível mover ao longo de um eixo
     * Corrige problemas de fronteira exata usando um pequeno epsilon
     */
    private double moveAxis(AABBd box, double dx, double dy, double dz) {
        double move = dx + dy + dz;
        AABBd moved = box.offset(new Vector3d(dx, dy, dz));

        // determinar limites com epsilon
        int minX = (int) Math.floor(Math.min(box.min.x, moved.min.x) + EPSILON);
        int maxX = (int) Math.floor(Math.max(box.max.x, moved.max.x) + 1 - EPSILON);
        int minY = (int) Math.floor(Math.min(box.min.y, moved.min.y) + EPSILON);
        int maxY = (int) Math.floor(Math.max(box.max.y, moved.max.y) + 1 - EPSILON);
        int minZ = (int) Math.floor(Math.min(box.min.z, moved.min.z) + EPSILON);
        int maxZ = (int) Math.floor(Math.max(box.max.z, moved.max.z) + 1 - EPSILON);

        double allowed = move;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!world.isSolid(x, y, z)) continue;

                    AABBd block = new AABBd(
                            new Vector3d(x, y, z),
                            new Vector3d(x + 1, y + 1, z + 1)
                    );

                    if (!moved.intersects(block)) continue;

                    if (dx > 0.0) allowed = Math.min(allowed, block.min.x - box.max.x);
                    else if (dx < 0.0) allowed = Math.max(allowed, block.max.x - box.min.x);
                    else if (dy > 0.0) allowed = Math.min(allowed, block.min.y - box.max.y);
                    else if (dy < 0.0) allowed = Math.max(allowed, block.max.y - box.min.y);
                    else if (dz > 0.0) allowed = Math.min(allowed, block.min.z - box.max.z);
                    else if (dz < 0.0) allowed = Math.max(allowed, block.max.z - box.min.z);
                }
            }
        }

        return Math.abs(allowed) > EPSILON ? allowed : 0.0;
    }

    /** empurra para fora se estiver dentro de blocos */
    private void resolvePenetration() {
        for (int attempt = 0; attempt < 3; attempt++) {
            AABBd box = getAABB();
            boolean collided = false;

            int minX = (int) Math.floor(box.min.x + EPSILON);
            int maxX = (int) Math.floor(box.max.x + 1 - EPSILON);
            int minY = (int) Math.floor(box.min.y + EPSILON);
            int maxY = (int) Math.floor(box.max.y + 1 - EPSILON);
            int minZ = (int) Math.floor(box.min.z + EPSILON);
            int maxZ = (int) Math.floor(box.max.z + 1 - EPSILON);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!world.isSolid(x, y, z)) continue;

                        AABBd block = new AABBd(
                                new Vector3d(x, y, z),
                                new Vector3d(x + 1, y + 1, z + 1)
                        );

                        if (!box.intersects(block)) continue;

                        collided = true;

                        double ox = Math.min(box.max.x, block.max.x) - Math.max(box.min.x, block.min.x);
                        double oy = Math.min(box.max.y, block.max.y) - Math.max(box.min.y, block.min.y);
                        double oz = Math.min(box.max.z, block.max.z) - Math.max(box.min.z, block.min.z);

                        // prioridade para eixo Y se estiver quase encostado
                        if (oy <= ox + EPSILON && oy <= oz + EPSILON) {
                            double dir = (position.y < block.min.y) ? -1 : 1;
                            position.y += dir * oy;
                        } else if (ox <= oz) {
                            double dir = (position.x < block.min.x) ? -1 : 1;
                            position.x += dir * ox;
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
