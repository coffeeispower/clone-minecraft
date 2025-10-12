package space.coffeeispower.minecraft_clone.ui;

import org.joml.Matrix4d;
import org.lwjgl.opengl.GL11;
import space.coffeeispower.minecraft_clone.resources.Resources;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.opengl.Draw;

public class Crosshair {
    public static void renderCrosshair(Camera camera) {
        var texture = Resources.INSTANCE.crosshairTexture;
        GL11.glDisable(GL11.GL_DEPTH);
        try (var ignored = texture.bindTextureOnSlot(0)) {
            Resources.INSTANCE.textureShader.setUniform("tex", 0);
            Draw.triangles(Resources.INSTANCE.imageModel, Resources.INSTANCE.textureShader, camera, new Matrix4d().scaleXY(texture.getWidth(), texture.getHeight()));
        }
        GL11.glEnable(GL11.GL_DEPTH);

    }
}
