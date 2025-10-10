package space.coffeeispower.entity;

import org.joml.Vector2i;
import org.joml.Vector3d;
import space.coffeeispower.world.Chunk;
import space.coffeeispower.world.World;


public abstract class Entity<T extends Entity<T>> {
    protected Vector3d position;
    protected Vector3d boundingBoxSize;
    protected final World world;
    protected Entity(Vector3d position, Vector3d boundingBoxSize, World world) {
        this.position = position;
        this.boundingBoxSize = boundingBoxSize;
        this.world = world;
    }

    public Vector3d getBoundingBoxSize() {
        return new Vector3d(boundingBoxSize);
    }

    public Vector3d getPosition() {
        return position;
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }
    public void setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
    }
    public Vector2i getCurrentChunkPosition() {
        return new Vector2i((int)(getPosition().x()/ Chunk.CHUNK_WIDTH), (int)(getPosition().z()/ Chunk.CHUNK_WIDTH));
    }
    public Chunk getCurrentChunk() {
        return world.getChunkAt(this.getCurrentChunkPosition());
    }
    public abstract void update(double deltaTime);
    public abstract EntityRenderer<T> getRenderer();
}
