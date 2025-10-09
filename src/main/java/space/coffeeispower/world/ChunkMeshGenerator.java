package space.coffeeispower.world;

import space.coffeeispower.opengl.model.Buffer;
import space.coffeeispower.opengl.model.BufferGroup;
import space.coffeeispower.opengl.texture.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public class ChunkMeshGenerator {

    public static BufferGroup generateMeshForChunk(Chunk chunk, TextureAtlas atlas) {
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

                    int baseVertex = vertices.size() / 3;

                    type.model().appendFaces(vertices, uvs, atlas,
                            top, bottom, left, right, front, back, x, y, z);

                }
            }
        }

        // Cria os buffers (posição e UVs)
        BufferGroup group = new BufferGroup();
        group.addBuffer(new Buffer(vertices.stream().mapToDouble((d) -> d).toArray(), 3)); // posições
        group.addBuffer(new Buffer(uvs.stream().mapToDouble((d) -> d).toArray(), 2)); // UVs
        return group;
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
