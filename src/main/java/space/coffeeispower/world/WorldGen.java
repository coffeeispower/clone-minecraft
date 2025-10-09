package space.coffeeispower.world;

import org.joml.SimplexNoise;


public final class WorldGen {
    public static void generateChunk(Chunk chunk) {
        for (int x = 0; x < Chunk.CHUNK_WIDTH; x++) {
            for (int z = 0; z < Chunk.CHUNK_WIDTH; z++) {
                int worldX = chunk.getPosition().x() * Chunk.CHUNK_WIDTH + x;
                int worldZ = chunk.getPosition().y() * Chunk.CHUNK_WIDTH + z;

                int height = getHeight(worldX, worldZ);

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

    private static int getHeight(int x, int z) {
        // Função de ruído simples
        double noise = SimplexNoise.noise(x * 0.05f, z * 0.05f); // normalizado entre -1 e 1
        int minHeight = 5;
        int maxHeight = 20;
        return minHeight + (int)((noise + 1) / 2 * (maxHeight - minHeight));
    }
}

