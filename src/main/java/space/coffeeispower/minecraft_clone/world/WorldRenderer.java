package space.coffeeispower.minecraft_clone.world;

import org.joml.Matrix4d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import space.coffeeispower.minecraft_clone.Resources;
import space.coffeeispower.minecraft_clone.entity.Entity;
import space.coffeeispower.minecraft_clone.entity.player.PlayerController;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.opengl.Draw;
import space.coffeeispower.minecraft_clone.opengl.texture.TextureAtlas;
import space.coffeeispower.minecraft_clone.window.Window;
import space.coffeeispower.minecraft_clone.world.block.BlockType;
import space.coffeeispower.minecraft_clone.world.chunk.Chunk;

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
                Resources.INSTANCE.textureShader.setUniform("tex", 0);
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

    public void renderBlockHighlight(PlayerController thePlayer, Window window, Camera camera) {
        var blockPosition = thePlayer.getHoveredBlock().blockPosition();
        var progress = thePlayer.getPlayer().getWorld().getBreakingProgress(blockPosition);
        Resources.INSTANCE.colorShader.setUniform("color", new Vector4f(5f / 255, 220f / 255, 240f / 255, (float) (0.3f + (progress * 0.7f))));
        Draw.triangles(window,
                Resources.INSTANCE.cubeModel,
                Resources.INSTANCE.colorShader,
                camera,
                new Matrix4d().translate(new Vector3f(blockPosition)).translate(-0.005, -0.005, -0.005).scale(1.01));
    }
    @Override
    public void close() {
        world.close();
        blockTextureAtlas.close();
    }
}
