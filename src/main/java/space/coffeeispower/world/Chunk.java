package space.coffeeispower.world;

import org.joml.Vector2i;
import org.joml.Vector3i;

public class Chunk {

    public static int CHUNK_WIDTH = 16;
    public static int CHUNK_HEIGHT = 256;
    private final Vector2i position;
    private final short[][][] blocks = new short[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];
    public Chunk(Vector2i position) {
        this.position = position;
    }

    public Chunk(int x, int y) {
        this(new Vector2i(x, y));
    }

    public BlockType getBlockAt(Vector3i p) {
        return getBlockAt(p.x, p.y, p.z);
    }

    public BlockType getBlockAt(int x, int y, int z) {
        var ordinal = blocks[x][y][z];
        return BlockType.values()[ordinal];
    }

    public void setBlockAt(int x, int y, int z, BlockType type) {
        blocks[x][y][z] = (short) type.ordinal();
    }

    public Vector2i getPosition() {
        return new Vector2i(position);
    }
}
