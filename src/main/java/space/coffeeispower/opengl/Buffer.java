package space.coffeeispower.opengl;

import java.io.Closeable;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class Buffer implements Closeable {

    final int vbo;
    final BufferType type;
    final int perVertexSize;

    public Buffer(int[] buffer, int perVertexSize) {

        if (perVertexSize > 0 && buffer.length % perVertexSize != 0) {
            throw new RuntimeException("O tamanho do buffer (" + buffer.length + ") não é divisivel pelo perVertexSize (" + perVertexSize + ")");
        }
        int[] vbo = new int[1];
        glGenBuffers(vbo);
        this.vbo = vbo[0];
        bind();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        type = BufferType.Int;
        unbindAll();
        this.perVertexSize = perVertexSize;

    }


    public Buffer(float[] buffer, int perVertexSize) {
        if (perVertexSize > 0 && buffer.length % perVertexSize != 0) {
            throw new RuntimeException("O tamanho do buffer (" + buffer.length + ") não é divisivel pelo perVertexSize (" + perVertexSize + ")");
        }
        int[] vbo = new int[1];
        glGenBuffers(vbo);
        this.vbo = vbo[0];
        bind();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        type = BufferType.Float;
        this.perVertexSize = perVertexSize;
        unbindAll();
    }

    public Buffer(double[] buffer, int perVertexSize) {
        if (perVertexSize > 0 && buffer.length % perVertexSize != 0) {
            throw new RuntimeException("O tamanho do buffer (" + buffer.length + ") não é divisivel pelo perVertexSize (" + perVertexSize + ")");
        }
        int[] vbo = new int[1];
        glGenBuffers(vbo);
        this.vbo = vbo[0];

        bind();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        type = BufferType.Double;
        this.perVertexSize = perVertexSize;
        unbindAll();
    }

    public static void unbindAll() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void bindToCurrentModel(int index) {
        bind();
        glVertexAttribPointer(index, perVertexSize, this.type.glType, false, 0, 0);
        glEnableVertexAttribArray(index);
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
    }

    @Override
    public void close() {
        glDeleteBuffers(this.vbo);
    }

    public enum BufferType {
        Int(GL_INT), Float(GL_FLOAT), Double(GL_DOUBLE);
        public final int glType;

        BufferType(int glType) {
            this.glType = glType;
        }
    }
}