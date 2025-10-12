package space.coffeeispower.minecraft_clone.opengl;

import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import space.coffeeispower.minecraft_clone.opengl.model.BufferGroup;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.Texture2D;
import space.coffeeispower.minecraft_clone.resources.Resources;

public final class Draw {
    private Draw() {
    }

    public static void triangles(BufferGroup bg, ShaderProgram shader, Camera camera, Matrix4d transform) {
        if (camera == null) camera = Camera.DEFAULT_CAMERA;
        try (var ignored = bg.bind()) {
            try (var ignored2 = shader.bind()) {
                shader.setUniform("viewMatrix", camera.toViewMatrix());
                shader.setUniform("projectionMatrix", camera.toProjectionMatrix());
                shader.setUniform("transformMatrix", transform);
                GL30.glDrawArrays(GL11.GL_TRIANGLES, 0, bg.verticesCount());
            }
        }
    }


    public static void texture(Texture2D texture, Camera camera, Matrix4d transform, ShaderProgram shader) {
        GL11.glDisable(GL11.GL_DEPTH);
        GL11.glDepthMask(false);
        try (var ignored = texture.bindTextureOnSlot(0)) {
            Resources.INSTANCE.textureShader.setUniform("tex", 0);
            Draw.triangles(Resources.INSTANCE.imageModel, shader == null ? Resources.INSTANCE.textureShader : shader, camera, transform != null ? transform : new Matrix4d().scaleXY(texture.getWidth(), texture.getHeight()));
        }
        GL11.glEnable(GL11.GL_DEPTH);
        GL11.glDepthMask(true);
    }

    public static void texture(Texture2D texture, Vector2d position, double rotationZ, ShaderProgram shader) {
        texture(texture, Camera.DEFAULT_UI_CAMERA, new Matrix4d().translate(position.x, position.y, 0).rotate(Math.toDegrees(rotationZ), 0, 0, 1), shader);
    }

    public static void texture(Texture2D texture, Matrix4d transform, ShaderProgram shader) {
        texture(texture, Camera.DEFAULT_UI_CAMERA, transform, shader);
    }

    public static void texture(Texture2D texture, Matrix4d transform) {
        texture(texture, Camera.DEFAULT_UI_CAMERA, transform, null);
    }

    public static void textureAtCenter(Texture2D texture, ShaderProgram shader) {
        texture(texture, null, shader);
    }

    public static void textureAtCenter(Texture2D texture) {
        texture(texture, (Matrix4d) null);
    }
}
