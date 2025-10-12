package space.coffeeispower.minecraft_clone.world;

import org.joml.Matrix4d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import space.coffeeispower.minecraft_clone.entity.Entity;
import space.coffeeispower.minecraft_clone.entity.player.PlayerController;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.opengl.Draw;
import space.coffeeispower.minecraft_clone.opengl.ShaderProgram;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.TextureAtlas;
import space.coffeeispower.minecraft_clone.resources.InfallibleAutoClose;
import space.coffeeispower.minecraft_clone.resources.Resources;
import space.coffeeispower.minecraft_clone.window.Window;
import space.coffeeispower.minecraft_clone.world.chunk.Chunk;

import java.io.IOException;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.glDepthMask;

public class WorldRenderer implements InfallibleAutoClose {
    private final static int TOTAL_BREAKING_STAGES = 10;
    private final World world;

    private final TextureAtlas breakingStagesAtlas;

    public WorldRenderer(World world) throws IOException {
        this.world = world;
        var breakingStagesPaths = IntStream
                .range(0, TOTAL_BREAKING_STAGES)
                .mapToObj(WorldRenderer::getTexturePathForBreakingStage).toArray(String[]::new);
        for (String breakingStagesPath : breakingStagesPaths) {
            System.out.println(breakingStagesPath);
        }
        breakingStagesAtlas = new TextureAtlas(breakingStagesPaths);
    }

    private static String getTexturePathForBreakingStage(int stage) {
        return "/textures/blocks/breakingStages/destroy_stage_" + stage + ".png";
    }

    private static int convertBreakingProgressToStage(double progress) {
        // garante que progress está entre 0 e 1
        progress = Math.max(0.0, Math.min(1.0, progress));
        return (int) (progress * (TOTAL_BREAKING_STAGES - 1));
    }


    private TextureAtlas.UVCoords getUVCoordsForBreakingStage(int stage) {
        return Resources.INSTANCE.blockTextureAtlas.getUV(getTexturePathForBreakingStage(stage));
    }


    public void renderWorld(Camera camera) {
        try (var ignored = Resources.INSTANCE.blockTextureAtlas.bindTextureOnSlot(0)) {
            for (var loadedChunk : world.getLoadedChunksIterable()) {
                var chunkPositionAsWorldCoord = loadedChunk.chunk().getPosition().mul(Chunk.CHUNK_WIDTH);
                Resources.INSTANCE.textureShader.setUniform("tex", 0);
                Draw.triangles(loadedChunk.model(), Resources.INSTANCE.textureShader, camera, new Matrix4d().translate(chunkPositionAsWorldCoord.x, 0, chunkPositionAsWorldCoord.y));
            }
        }
    }

    public void renderEntities(Window window, Camera camera) {
        for (Entity<?> entity : world.getEntities()) {
            var renderer = entity.getRenderer();
            if (renderer == null) continue;
            renderer.render(camera);
        }
    }

    public void renderBlockHighlight(PlayerController thePlayer, Camera camera) {
        var hoveredBlock = thePlayer.getHoveredBlock();
        if (hoveredBlock == null) return;
        var blockPosition = hoveredBlock.blockPosition();
        Resources.INSTANCE.colorShader.setUniform("color", new Vector4f(5f / 255, 220f / 255, 240f / 255, 0.1f));
        glDepthMask(false);

        Draw.triangles(
                Resources.INSTANCE.cubeModel,
                Resources.INSTANCE.colorShader,
                camera,
                new Matrix4d().translate(new Vector3f(blockPosition)).translate(-0.005, -0.005, -0.005).scale(1.01));
        glDepthMask(true);
    }

    public void renderBlockBreaking(PlayerController thePlayer, Camera camera) {
        var breakingBlock = thePlayer.getBreakingBlock();
        if (breakingBlock == null) return;
        var blockPosition = breakingBlock.position();
        var stage = convertBreakingProgressToStage(breakingBlock.progress());
        System.out.println("stage: " + stage);
        ShaderProgram breakingStagesShader = Resources.INSTANCE.breakingStagesShader;
        breakingStagesShader.setUniform("tex", 0);
        breakingStagesShader.setUniform("breakStage", stage);
        glDepthMask(false);
        try (var ignored = breakingStagesAtlas.bindTextureOnSlot(0)) {
            Draw.triangles(
                    Resources.INSTANCE.cubeModel,
                    breakingStagesShader,
                    camera,
                    new Matrix4d().translate(new Vector3f(blockPosition)).translate(-0.005, -0.005, -0.005).scale(1.01));
        }
        glDepthMask(true);
    }

    @Override
    public void close() {
        world.close();
    }
}
