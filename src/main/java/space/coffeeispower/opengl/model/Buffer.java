package space.coffeeispower.opengl.model;

import space.coffeeispower.opengl.BindGuard;

import java.io.Closeable;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public final class Buffer implements Closeable {

    private final int vbo;
    private final BufferType type;
    private final int dimensions;
    private int size;
    private boolean closed;
    public Buffer(int[] buffer, int dimensions) {

        if (dimensions > 0 && buffer.length % dimensions != 0) {
            throw new RuntimeException("O tamanho do buffer (" + buffer.length + ") não é divisivel pelas dimensões (" + dimensions + ")");
        }
        int[] vbo = new int[1];
        glGenBuffers(vbo);
        this.vbo = vbo[0];
        try (var ignored = bind()) {
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
        type = BufferType.Int;
        this.dimensions = dimensions;
        this.size = buffer.length;
    }


    public Buffer(float[] buffer, int dimensions) {
        if (dimensions > 0 && buffer.length % dimensions != 0) {
            throw new RuntimeException("O tamanho do buffer (" + buffer.length + ") não é divisivel pelo perVertexSize (" + dimensions + ")");
        }
        int[] vbo = new int[1];
        glGenBuffers(vbo);
        this.vbo = vbo[0];
        try (var ignored = bind()) {
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
        type = BufferType.Float;
        this.dimensions = dimensions;
        this.size = buffer.length;
    }

    public Buffer(double[] buffer, int dimensions) {
        if (dimensions > 0 && buffer.length % dimensions != 0) {
            throw new RuntimeException("O tamanho do buffer (" + buffer.length + ") não é divisivel pelo perVertexSize (" + dimensions + ")");
        }
        int[] vbo = new int[1];
        glGenBuffers(vbo);
        this.vbo = vbo[0];

        try (var ignored = bind()) {
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
        type = BufferType.Double;
        this.dimensions = dimensions;
        this.size = buffer.length;
    }

    void bindToCurrentModel(int index) {
        try (var ignored = bind()) {
            glVertexAttribPointer(index, dimensions, this.type.glType, false, 0, 0);
        }
        glEnableVertexAttribArray(index);
    }

    public BindGuard bind() {
        return new BindGuard(vbo, (i) -> glBindBuffer(GL_ARRAY_BUFFER, i), GL_ARRAY_BUFFER_BINDING);
    }

    @Override
    public void close() {
        if(!closed)
            glDeleteBuffers(this.vbo);
        closed = true;
    }

    public int size() {
        return size/Math.max(this.dimensions, 1);
    }

    public int getDimensions() {
        return dimensions;
    }

    public enum BufferType {
        Int(GL_INT), Float(GL_FLOAT), Double(GL_DOUBLE);
        public final int glType;

        BufferType(int glType) {
            this.glType = glType;
        }
    }

    public int id() {
        return vbo;
    }
    private void checkBufferSize(int bufferSize) {
        if (dimensions > 0 && bufferSize % dimensions != 0) {
            throw new RuntimeException("O tamanho do buffer (" + bufferSize + ") não é divisivel pelo perVertexSize (" + dimensions + ")");
        }
    }
    public void updateBuffer(int[] buffer) {
        checkBufferSize(buffer.length);
        try (var ignored = bind()) {
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
        size = buffer.length;
    }
    public void updateBuffer(float[] buffer) {
        checkBufferSize(buffer.length);
        try (var ignored = bind()) {
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
        size = buffer.length;
    }
    public void updateBuffer(double[] buffer) {
        checkBufferSize(buffer.length);
        try (var ignored = bind()) {
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        }
        size = buffer.length;
    }

    public BufferType type() {
        return type;
    }
}