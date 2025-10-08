package space.coffeeispower.opengl;

import org.joml.Matrix4d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import space.coffeeispower.opengl.model.BufferGroup;
import space.coffeeispower.window.Window;

public final class Draw {
    private Draw() {}
    public static void triangles(Window window, BufferGroup bg, ShaderProgram shader, Camera camera, Matrix4d transform) {
        if(camera == null) camera = Camera.DEFAULT_CAMERA;
        try (var ignored = bg.bind()) {
            try (var ignored2 = shader.bind()) {
                shader.setUniform("viewMatrix", camera.toViewMatrix());
                shader.setUniform("projectionMatrix", camera.toProjectionMatrix(window));
                shader.setUniform("transformMatrix", transform);
                GL30.glDrawArrays(GL11.GL_TRIANGLES, 0, bg.verticesCount());
            }
        }
    }
}
