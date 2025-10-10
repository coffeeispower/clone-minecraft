package space.coffeeispower.opengl.model;


import org.lwjgl.opengl.GL30;
import space.coffeeispower.opengl.BindGuard;

import java.io.Closeable;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;

public final class BufferGroup implements Closeable {
    private final int vao;
    private final ArrayList<Buffer> buffers = new ArrayList<>();

    public BufferGroup(Buffer ...buffers) {
        vao = glGenVertexArrays();
        for (Buffer buffer : buffers) {
            addBuffer(buffer);
        }
    }

    public BindGuard bind() {
        return new BindGuard(vao, GL30::glBindVertexArray, GL_VERTEX_ARRAY_BINDING);
    }

    public int verticesCount() {
        return buffers.stream().mapToInt(Buffer::size).max().orElse(0);
    }


    public int addBuffer(Buffer buffer) {
        var newIndex = buffers.size();
        try (var ignored = bind()) {
            buffer.bindToCurrentModel(newIndex);
            buffers.add(buffer);
        }
        return newIndex;
    }

    public void replaceBuffer(Buffer buffer, int index) {
        if (index >= buffers.size()) {
            throw new IndexOutOfBoundsException(index);
        }
        try (var ignored = bind()) {
            buffer.bindToCurrentModel(index);
            buffers.set(index, buffer).close();
        }
    }
    public Buffer getBuffer(int index) {
        return buffers.get(index);
    }
    public int id() {
        return vao;
    }
    private boolean closed;
    @Override
    public void close() {
        if(!closed) {
            glDeleteVertexArrays(this.vao);
            for (Buffer buffer : buffers) {
                buffer.close();
            }
        }
        closed = true;

    }
}
