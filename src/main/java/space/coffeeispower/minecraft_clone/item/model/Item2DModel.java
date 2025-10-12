package space.coffeeispower.minecraft_clone.item.model;

import org.joml.Matrix4d;
import org.joml.Vector2i;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.opengl.Draw;
import space.coffeeispower.minecraft_clone.opengl.model.BufferGroup;
import space.coffeeispower.minecraft_clone.opengl.texture.ImageData;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.Framebuffer;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.ImageTexture;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.Texture2D;
import space.coffeeispower.minecraft_clone.resources.Resources;
import space.coffeeispower.minecraft_clone.window.Window;

import java.io.IOException;

public class Item2DModel implements ItemModel {
    private static Framebuffer firstPersonRenderOverlayFb;
    private final Camera threedCamera = new Camera(new Camera.Perspective(70));
    private final Texture2D texture;
    private final BufferGroup extruded2dModel;
    private final Vector2i textureSize;

    public Item2DModel(String texturePath) throws IOException {
        try (var data = ImageData.loadImageDataFromResource(texturePath)) {
            textureSize = new Vector2i(data.width(), data.height());
            texture = new ImageTexture(data);
            extruded2dModel = Texture2Voxel.generateMeshForTexture(data);
        }
    }

    @Override
    public void renderAsUi(Matrix4d transform) {
        Draw.texture(texture, transform);
    }

    @Override
    public void renderInFirstPersonView(Matrix4d transform, Camera.Perspective perspective, Window window) {
        threedCamera.setMode(perspective);
        firstPersonRenderOverlayFb = Framebuffer.adjustSizeToWindow(firstPersonRenderOverlayFb, window);
        try (var ignored = firstPersonRenderOverlayFb.bindForDrawing();
             var ignored1 = texture.bindTextureOnSlot(0)) {
            Resources.INSTANCE.items2DShader.setUniform("tex", 0);
            Draw.triangles(extruded2dModel, Resources.INSTANCE.items2DShader, threedCamera,
                    transform.translate(0.7, -1.1, -1.2f, new Matrix4d())
                            .rotateXYZ(Math.toRadians(-30), Math.toRadians(-83), Math.toRadians(-4))
                            .rotateX(Math.toRadians(-90))
                            .scale(1.0 / Math.max(textureSize.x, textureSize.y)));
        }
        Draw.textureAtCenter(firstPersonRenderOverlayFb);
    }

    @Override
    public void close() {
        texture.close();
        extruded2dModel.close();
    }
}
