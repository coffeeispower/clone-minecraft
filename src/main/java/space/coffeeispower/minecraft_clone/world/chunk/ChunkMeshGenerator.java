package space.coffeeispower.minecraft_clone.world.chunk;

import space.coffeeispower.minecraft_clone.opengl.texture.TextureAtlas;
import space.coffeeispower.minecraft_clone.world.block.BlockType;

import java.util.ArrayList;
import java.util.List;

public class ChunkMeshGenerator {
    public record ChunkMesh(double[] vertices, double[] uv) {}
    public static ChunkMesh generateMeshForChunk(Chunk chunk, TextureAtlas atlas) {
        List<Double> vertices = new ArrayList<>();
        List<Double> uvs = new ArrayList<>();

        for (int x = 0; x < Chunk.CHUNK_WIDTH; x++) {
            for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                for (int z = 0; z < Chunk.CHUNK_WIDTH; z++) {

                    BlockType type = chunk.getBlockAt(x, y, z);
                    if (type.model() == null)
                        continue; // ignora blocos de ar

                    boolean top = isFaceVisible(chunk, x, y + 1, z);
                    boolean bottom = isFaceVisible(chunk, x, y - 1, z);
                    boolean left = isFaceVisible(chunk, x - 1, y, z);
                    boolean right = isFaceVisible(chunk, x + 1, y, z);
                    boolean front = isFaceVisible(chunk, x, y, z + 1);
                    boolean back = isFaceVisible(chunk, x, y, z - 1);

                    if (!(top || bottom || left || right || front || back))
                        continue; // bloco completamente oculto


                    type.model().appendFaces(vertices, uvs, atlas,
                            top, bottom, left, right, front, back, x, y, z);

                }
            }
        }

        return new ChunkMesh(vertices.stream().mapToDouble((d) -> d).toArray(), uvs.stream().mapToDouble((d) -> d).toArray());
    }

    private static boolean isFaceVisible(Chunk chunk, int x, int y, int z) {
        // Fora dos limites => face visível
        if (x < 0 || x >= Chunk.CHUNK_WIDTH ||
                y < 0 || y >= Chunk.CHUNK_HEIGHT ||
                z < 0 || z >= Chunk.CHUNK_WIDTH)
            return true;

        BlockType neighbor = chunk.getBlockAt(x, y, z);
        return neighbor.model() == null;
    }

}
