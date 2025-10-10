package space.coffeeispower.world;

import org.joml.Matrix4d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import space.coffeeispower.entity.Entity;
import space.coffeeispower.models.Resources;
import space.coffeeispower.opengl.Camera;
import space.coffeeispower.opengl.Draw;
import space.coffeeispower.opengl.texture.TextureAtlas;
import space.coffeeispower.window.Window;

import java.io.IOException;

public class WorldRenderer implements AutoCloseable{
    private final World world;
    private final TextureAtlas blockTextureAtlas;

    public WorldRenderer(World world) throws IOException {
        this.world = world;
        blockTextureAtlas = new TextureAtlas(BlockType.getAllTexturesPaths().toArray(new String[0]));
    }

    public TextureAtlas getBlockTextureAtlas() {
        return blockTextureAtlas;
    }

    public void renderWorld(Window window, Camera camera) {
        for(var loadedChunk: world.getLoadedChunksIterable()) {
            var chunkPositionAsWorldCoord = loadedChunk.chunk().getPosition().mul(Chunk.CHUNK_WIDTH);
            try (var ignored = blockTextureAtlas.texture().bind(0)) {
                Resources.INSTANCE.textureShader.setUniform("textureAtlas", 0);
                Draw.triangles(window, loadedChunk.model(), Resources.INSTANCE.textureShader, camera, new Matrix4d().translate(chunkPositionAsWorldCoord.x, 0, chunkPositionAsWorldCoord.y));
            }
        }
    }

    public void renderEntities(Window window, Camera camera) {
        for (Entity<?> entity : world.getEntities()) {
            var renderer = entity.getRenderer();
            if(renderer == null) continue;
            renderer.render(window, camera);
        }
    }
    public void renderBlockHighlight(Vector3i blockPosition, Window window, Camera camera) {
        Resources.INSTANCE.colorShader.setUniform("color", new Vector4f(5f/255, 220f/255, 240f/255, 0.3f));
        Draw.triangles(window,
                Resources.INSTANCE.cubeModel,
                Resources.INSTANCE.colorShader,
                camera,
                new Matrix4d().translate(new Vector3f(blockPosition)).scale(1.01));
    }
    @Override
    public void close() {
        world.close();
        blockTextureAtlas.close();
    }
}
