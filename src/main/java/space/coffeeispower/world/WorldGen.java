package space.coffeeispower.world;

import org.lwjgl.stb.STBPerlin;


public final class WorldGen {
    public static void generateChunk(Chunk chunk, int seed) {
        for (int x = 0; x < Chunk.CHUNK_WIDTH; x++) {
            for (int z = 0; z < Chunk.CHUNK_WIDTH; z++) {
                int worldX = chunk.getPosition().x() * Chunk.CHUNK_WIDTH + x;
                int worldZ = chunk.getPosition().y() * Chunk.CHUNK_WIDTH + z;

                int height = getHeight(worldX, worldZ,  seed);

                for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                    if (y > height) {
                        chunk.setBlockAt(x, y, z, BlockType.Air);
                    } else if (y == 0) {
                        chunk.setBlockAt(x, y, z, BlockType.Bedrock);
                    } else if (y < height - 3) {
                        chunk.setBlockAt(x, y, z, BlockType.Stone);
                    } else if (y < height) {
                        chunk.setBlockAt(x, y, z, BlockType.Dirt);
                    } else {
                        chunk.setBlockAt(x, y, z, BlockType.Grass);
                    }
                }
            }
        }
    }

    private static int getHeight(int x, int z, int seed) {
        // Função de ruído simples
        double noise = STBPerlin.stb_perlin_noise3_seed(x * 0.05f, 0,  z * 0.05f, 0, 0, 0, seed); // normalizado entre -1 e 1
        int minHeight = 5;
        int maxHeight = 20;
        return minHeight + (int)((noise + 1) / 2 * (maxHeight - minHeight));
    }
}

