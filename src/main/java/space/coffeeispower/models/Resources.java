package space.coffeeispower.models;

import space.coffeeispower.opengl.ShaderProgram;
import space.coffeeispower.opengl.model.Buffer;
import space.coffeeispower.opengl.model.BufferGroup;

public class Resources {
    public static Resources INSTANCE;

    public Resources() throws Exception {
    }

    public static void init() {
        try {
            INSTANCE = new Resources();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final BufferGroup cubeModel = new BufferGroup(
        new Buffer(new double[] {
                // Frente
                -0.5f, -0.5f,  0.5f,
                0.5f, -0.5f,  0.5f,
                0.5f,  0.5f,  0.5f,
                0.5f,  0.5f,  0.5f,
                -0.5f,  0.5f,  0.5f,
                -0.5f, -0.5f,  0.5f,

                // Trás
                -0.5f, -0.5f, -0.5f,
                -0.5f,  0.5f, -0.5f,
                0.5f,  0.5f, -0.5f,
                0.5f,  0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,

                // Esquerda
                -0.5f,  0.5f,  0.5f,
                -0.5f,  0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f,  0.5f,
                -0.5f,  0.5f,  0.5f,

                // Direita
                0.5f,  0.5f,  0.5f,
                0.5f, -0.5f,  0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f,  0.5f, -0.5f,
                0.5f,  0.5f,  0.5f,

                // Topo
                -0.5f,  0.5f, -0.5f,
                -0.5f,  0.5f,  0.5f,
                0.5f,  0.5f,  0.5f,
                0.5f,  0.5f,  0.5f,
                0.5f,  0.5f, -0.5f,
                -0.5f,  0.5f, -0.5f,

                // Fundo
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -0.5f,
                0.5f, -0.5f,  0.5f,
                0.5f, -0.5f,  0.5f,
                -0.5f, -0.5f,  0.5f,
                -0.5f, -0.5f, -0.5f
        }, 3)
    );
    public final ShaderProgram colorShader = new ShaderProgram("/shaders/simpleColor/vertex.glsl", "/shaders/simpleColor/fragment.glsl");
    public final ShaderProgram textureShader = new ShaderProgram("/shaders/texturesModel/vertex.glsl", "/shaders/texturesModel/fragment.glsl");
}
