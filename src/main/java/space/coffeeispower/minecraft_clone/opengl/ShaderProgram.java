package space.coffeeispower.minecraft_clone.opengl;

import org.joml.Matrix4d;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;

public final class ShaderProgram implements Closeable {
    private final int programId;

    private void linkProgram() {
        glLinkProgram(programId);
        int status = glGetProgrami(programId, GL_LINK_STATUS);
        String log = glGetProgramInfoLog(programId);

        if (!log.isBlank()) {
            System.err.println("[Program Log]:\n" + log);
        }

        if (status == GL_FALSE) {
            throw new RuntimeException("Erro ao linkar programa:\n" + log);
        }
    }
    public ShaderProgram(String vertexResource, String fragmentResource) throws Exception {
        // Ler código GLSL a partir dos resources
        String vertexSource = readResource(vertexResource);
        String fragmentSource = readResource(fragmentResource);
        // Compilar shaders
        int vertexShader = compileShader(vertexSource, GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentSource, GL_FRAGMENT_SHADER);

        // Criar e linkar o programa
        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        linkProgram();
        // apagar shaders individuais (já linkados)
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private String readResource(String resourcePath) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new RuntimeException("Shader não encontrado: " + resourcePath);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new Exception("Erro a ler shader resource: " + resourcePath, e);
        }
    }

    private int compileShader(String source, int type) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);

        // verificar estado e log
        int status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        String log = glGetShaderInfoLog(shaderId);

        if (!log.isBlank()) {
            System.err.println("[Shader Log] " + shaderTypeName(type) + ":\n" + log);
        }

        if (status == GL_FALSE) {
            throw new RuntimeException("Falha a compilar " + shaderTypeName(type) + ":\n" + log);
        }

        return shaderId;
    }

    private String shaderTypeName(int type) {
        return switch (type) {
            case GL_VERTEX_SHADER -> "Vertex Shader";
            case GL_FRAGMENT_SHADER -> "Fragment Shader";
            default -> "Shader Desconhecido";
        };
    }

    public BindGuard bind() {
        return new BindGuard(programId, GL30::glUseProgram, GL_CURRENT_PROGRAM);
    }

    @Override
    public void close() {
        glDeleteProgram(programId);
    }

    public void setUniform(String name, Vector2i v) {
        try (var ignored = bind()) {
            int loc = glGetUniformLocation(programId, name);
            glUniform2f(loc, v.x, v.y);
        }
    }
    public void setUniform(String name, Vector3f v) {
        try (var ignored = bind()) {
            int loc = glGetUniformLocation(programId, name);
            glUniform3f(loc, v.x, v.y, v.z);
        }
    }

    public void setUniform(String name, Vector4f v) {
        try (var ignored = bind()) {
            int loc = glGetUniformLocation(programId, name);
            glUniform4f(loc, v.x, v.y, v.z, v.w);
        }
    }

    public void setUniform(String name, float value) {
        try (var ignored = bind()) {
            int loc = glGetUniformLocation(programId, name);
            glUniform1f(loc, value);
        }
    }

    public void setUniform(String name, int value) {
        try (var ignored = bind()) {
            int loc = glGetUniformLocation(programId, name);
            glUniform1i(loc, value);
        }
    }
    public void setUniform(String name, Matrix4d m) {
        try (var ignored = bind()){
            try (var s = MemoryStack.stackPush()) {
                var buffer = m.get(s.mallocFloat(4 * 4));
                int loc = glGetUniformLocation(programId, name);
                glUniformMatrix4fv(loc, false, buffer);
            }
        }
    }
    public int id() {
        return programId;
    }
}
