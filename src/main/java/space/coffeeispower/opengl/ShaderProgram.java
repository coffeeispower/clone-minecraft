package space.coffeeispower.opengl;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ShaderProgram {
    private final int programId;

    private void linkProgram() {
        glLinkProgram(programId);
        int status = glGetProgrami(programId, GL_LINK_STATUS);
        String log = glGetProgramInfoLog(programId);

        if (log != null && !log.isBlank()) {
            System.err.println("[Program Log]:\n" + log);
        }

        if (status == GL_FALSE) {
            throw new RuntimeException("Erro ao linkar programa:\n" + log);
        }
    }
    public ShaderProgram(String vertexResource, String fragmentResource) {
        // 1️⃣ Ler código GLSL a partir dos resources
        String vertexSource = readResource(vertexResource);
        String fragmentSource = readResource(fragmentResource);
        // 2️⃣ Compilar shaders
        int vertexShader = compileShader(vertexSource, GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentSource, GL_FRAGMENT_SHADER);

        // 3️⃣ Criar e linkar o programa
        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        linkProgram();
        // apagar shaders individuais (já linkados)
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private String readResource(String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new RuntimeException("Shader não encontrado: " + resourcePath);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro a ler shader resource: " + resourcePath, e);
        }
    }

    private int compileShader(String source, int type) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);

        // verificar estado e log
        int status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        String log = glGetShaderInfoLog(shaderId);

        if (log != null && !log.isBlank()) {
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

    public void bind() {
        glUseProgram(programId);
    }

    public static void unbindAll() {
        glUseProgram(0);
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }

    public void setUniform(String name, float v1, float v2, float v3) {
        int loc = glGetUniformLocation(programId, name);
        glUniform3f(loc, v1, v2, v3);
    }

    public void setUniform(String name, float v1, float v2, float v3, float v4) {
        int loc = glGetUniformLocation(programId, name);
        glUniform4f(loc, v1, v2, v3, v4);
    }

    public void setUniform(String name, float value) {
        int loc = glGetUniformLocation(programId, name);
        glUniform1f(loc, value);
    }

    public void setUniform(String name, int value) {
        int loc = glGetUniformLocation(programId, name);
        glUniform1i(loc, value);
    }

    public int getId() {
        return programId;
    }
}
