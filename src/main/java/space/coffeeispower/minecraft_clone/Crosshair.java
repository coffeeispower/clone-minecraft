package space.coffeeispower.minecraft_clone;

import org.joml.Matrix4d;
import org.lwjgl.opengl.GL11;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.opengl.Draw;
import space.coffeeispower.minecraft_clone.window.Window;

public class Crosshair {
    public static void renderCrosshair(Camera camera, Window window) {
        var texture = Resources.INSTANCE.crosshairTexture;
        GL11.glDisable(GL11.GL_DEPTH);
        try (var ignored = texture.bind(0)) {
            Resources.INSTANCE.textureShader.setUniform("tex", 0);
            Draw.triangles(window, Resources.INSTANCE.imageModel, Resources.INSTANCE.textureShader, camera, new Matrix4d().scaleXY(texture.getWidth(), texture.getWidth()));
        }
        GL11.glEnable(GL11.GL_DEPTH);

    }
}
