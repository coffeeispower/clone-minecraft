package space.coffeeispower.minecraft_clone.item.model;

import org.joml.Matrix4d;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.opengl.Draw;
import space.coffeeispower.minecraft_clone.opengl.ShaderProgram;
import space.coffeeispower.minecraft_clone.opengl.model.BufferGroup;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.Framebuffer;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.Texture2D;
import space.coffeeispower.minecraft_clone.resources.Resources;
import space.coffeeispower.minecraft_clone.window.Window;
import space.coffeeispower.minecraft_clone.world.block.BlockType;

public class BlockItemModel implements ItemModel {

    private final static Matrix4d diagonalViewMatrix = new Matrix4d()
            .rotateXYZ(Math.toRadians(35.264), Math.toRadians(45), 0);
    private static Framebuffer firstPersonRenderOverlayFb;
    private final BlockType block;
    private final BufferGroup blockModel;
    private final Texture2D blockIsometricProjection;
    private static final int ISOMETRIC_TEXTURE_SIZE = 64;

    public BlockItemModel(BlockType block) {
        this.block = block;
        if (block.model() == null) {
            throw new RuntimeException("Itens de bloco precisam de um BlockModel: O bloco " + block.name() + " não tem um BlockModel");
        }
        blockModel = block.model().createCubeModel();
        var texture = Resources.INSTANCE.blockTextureAtlas;
        //noinspection resource: o framebuffer é dropado quando a função dropFramebuffer é chamada
        var blockIsometricProjectionFb = new Framebuffer(ISOMETRIC_TEXTURE_SIZE, ISOMETRIC_TEXTURE_SIZE);
        try (var ignored = blockIsometricProjectionFb.bindForDrawing(); var ignored2 = texture.bindTextureOnSlot(0)) {
            ShaderProgram shader = Resources.INSTANCE.textureShader;
            shader.setUniform("tex", 0);
            Draw.triangles(blockModel, shader, new Camera(new Camera.Orthogonal()), diagonalViewMatrix);
        }
        blockIsometricProjection = blockIsometricProjectionFb.dropFramebuffer();
    }

    @Override
    public void renderAsUi(Matrix4d transform) {
        // Desenhar a textura gerada no inventário
        Draw.texture(blockIsometricProjection, transform.scaleXY(blockIsometricProjection.getWidth(), blockIsometricProjection.getHeight(), new Matrix4d()));
    }

    @Override
    public void renderInFirstPersonView(Matrix4d transform, Camera.Perspective perspective, Window window) {
        // Criar a câmera 3D do jogador
        Camera camera = new Camera(perspective);

        // Ajustar ou criar framebuffer overlay do tamanho da janela
        firstPersonRenderOverlayFb = Framebuffer.adjustSizeToWindow(firstPersonRenderOverlayFb, window);

        try (var fbBind = firstPersonRenderOverlayFb.bindForDrawing();
             var texBind = Resources.INSTANCE.blockTextureAtlas.bindTextureOnSlot(0)) {

            ShaderProgram shader = Resources.INSTANCE.textureShader;
            shader.setUniform("tex", 0);

            // Transform base do bloco: posicionar no canto inferior direito da tela
            Matrix4d itemTransform = new Matrix4d(transform)
                    .translate(1, -0.8, -1.4)
                    .rotateXYZ(Math.toRadians(8), Math.toRadians(25), 0)
                    .scale(0.8);

            // Renderizar o bloco 3D no framebuffer
            Draw.triangles(blockModel, shader, camera, itemTransform);
        }

        // Desenhar o framebuffer overlay como textura na tela
        Draw.textureAtCenter(firstPersonRenderOverlayFb);
    }


    @Override
    public void close() {
        blockModel.close();
        blockIsometricProjection.close();
    }
}
