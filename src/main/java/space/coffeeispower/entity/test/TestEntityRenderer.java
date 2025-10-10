package space.coffeeispower.entity.test;

import org.joml.Matrix4d;
import space.coffeeispower.entity.EntityRenderer;
import space.coffeeispower.opengl.Camera;
import space.coffeeispower.opengl.Draw;
import space.coffeeispower.opengl.ShaderProgram;
import space.coffeeispower.opengl.model.Buffer;
import space.coffeeispower.opengl.model.BufferGroup;
import space.coffeeispower.window.Window;

public class TestEntityRenderer implements EntityRenderer<TestEntity> {

    private final TestEntity entity;
    private static class Resources {
        private static Resources INSTANCE;

        public static Resources get() {
            if(INSTANCE == null) {
                try {
                    INSTANCE = new Resources();
                } catch (Exception e) {
                    throw new RuntimeException("Falha ao inicializar o renderizador da entidade de teste", e);
                }
            }
            return INSTANCE;
        }
        private final BufferGroup model;
        private final ShaderProgram colorShader;
        private Resources() throws Exception {
            model = new BufferGroup();
            model.addBuffer(new Buffer(new double[] {
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
            }, 3));
            colorShader = new ShaderProgram("/shaders/simpleColor/vertex.glsl", "/shaders/simpleColor/fragment.glsl");
        }
    }
    TestEntityRenderer(TestEntity entity) {
        this.entity = entity;
    }
    @Override
    public TestEntity getEntity() {
        return entity;
    }

    @Override
    public void render(Window window, Camera camera) {
        var boxSize = entity.getBoundingBoxSize();
        var transformMatrix = new Matrix4d().translate(entity.getPosition()).scale(boxSize);
        var resources = Resources.get();
        try(var ignored = resources.colorShader.bind()) {
            resources.colorShader.setUniform("color", entity.color);
            Draw.triangles(window, resources.model, resources.colorShader, camera, transformMatrix);
        }
    }

}
