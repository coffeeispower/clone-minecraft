package space.coffeeispower.world;

import org.joml.Matrix4d;
import space.coffeeispower.opengl.Camera;
import space.coffeeispower.opengl.Draw;
import space.coffeeispower.opengl.ShaderProgram;
import space.coffeeispower.opengl.texture.TextureAtlas;
import space.coffeeispower.window.Window;

public class WorldRenderer {

    public static void renderWorld(World world, TextureAtlas blockTextureAtlas, Window window, ShaderProgram shader, Camera camera) {
        for(var loadedChunk: world.getLoadedChunksIterable()) {
            var chunkPositionAsWorldCoord = loadedChunk.chunk().getPosition().mul(Chunk.CHUNK_WIDTH);
            try (var ignored = blockTextureAtlas.texture().bind(0)) {
                shader.setUniform("textureAtlas", 0);
                Draw.triangles(window, loadedChunk.model(), shader, camera, new Matrix4d().translate(chunkPositionAsWorldCoord.x, 0, chunkPositionAsWorldCoord.y));
            }
        }
    }
}
