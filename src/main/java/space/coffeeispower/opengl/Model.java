package space.coffeeispower.opengl;


import org.lwjgl.opengl.GL30;

import java.io.Closeable;

import static org.lwjgl.opengl.GL30.*;

public class Model implements Closeable {
    final int vao;

    public Model() {
        vao = glGenVertexArrays();
    }
    public void bind() {
        glBindVertexArray(vao);
    }
    public static void unbindAll() {
        glBindVertexArray(0);
    }
    @Override
    public void close() {
        glDeleteVertexArrays(this.vao);
    }
}
