package space.coffeeispower.minecraft_clone.resources;

import space.coffeeispower.minecraft_clone.opengl.ShaderProgram;
import space.coffeeispower.minecraft_clone.opengl.model.Buffer;
import space.coffeeispower.minecraft_clone.opengl.model.BufferGroup;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.ImageTexture;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.Texture2D;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.TextureAtlas;
import space.coffeeispower.minecraft_clone.world.block.BlockType;

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
            new Buffer(new double[]{
                    // Frente
                    0, 0, 1,
                    1, 0, 1,
                    1, 1, 1,
                    1, 1, 1,
                    0, 1, 1,
                    0, 0, 1,

                    // Trás
                    0, 0, 0,
                    0, 1, 0,
                    1, 1, 0,
                    1, 1, 0,
                    1, 0, 0,
                    0, 0, 0,

                    // Esquerda
                    0, 1, 1,
                    0, 1, 0,
                    0, 0, 0,

                    0, 0, 0,
                    0, 0, 1,
                    0, 1, 1,

                    // Direita

                    1, 1, 0,
                    1, 1, 1,
                    1, 0, 0,

                    1, 0, 1,
                    1, 0, 0,
                    1, 1, 1,

                    // Topo
                    0, 1, 0,
                    0, 1, 1,
                    1, 1, 1,
                    1, 1, 1,
                    1, 1, 0,
                    0, 1, 0,

                    // Fundo
                    0, 0, 0,
                    1, 0, 0,
                    1, 0, 1,
                    1, 0, 1,
                    0, 0, 1,
                    0, 0, 0
            }, 3),

            // Coordenadas de textura (UVs)
            new Buffer(new double[]{
                    // Frente
                    0, 0,
                    1, 0,
                    1, 1,
                    1, 1,
                    0, 1,
                    0, 0,

                    // Trás
                    0, 0,
                    0, 1,
                    1, 1,
                    1, 1,
                    1, 0,
                    0, 0,

                    // Esquerda
                    1, 1,
                    0, 1,
                    0, 0,

                    0, 0,
                    1, 0,
                    1, 1,

                    // Direita
                    1, 1,
                    0, 1,
                    1, 0,

                    0, 0,
                    1, 0,
                    0, 1,

                    // Topo
                    0, 0,
                    1, 0,
                    1, 1,
                    1, 1,
                    0, 1,
                    0, 0,

                    // Fundo
                    1, 0,
                    0, 0,
                    0, 1,

                    0, 1,
                    1, 1,
                    1, 0
            }, 2)
    );

    public final BufferGroup rectangleModel = new BufferGroup(
            new Buffer(new double[]{
                    -0.5f, -0.5f, 0,
                    0.5f, -0.5f, 0,
                    0.5f, 0.5f, 0,
                    0.5f, 0.5f, 0,
                    -0.5f, 0.5f, 0,
                    -0.5f, -0.5f, 0
            }, 3),
            new Buffer(new double[]{
                    0, 0,
                    1, 0,
                    1, 1,
                    1, 1,
                    0, 1,
                    0, 0
            }, 2)
    );
    public final Texture2D crosshairTexture = new ImageTexture("/textures/crosshair.png");
    public final ShaderProgram colorShader = new ShaderProgram("/shaders/simpleColor/vertex.glsl", "/shaders/simpleColor/fragment.glsl");
    public final ShaderProgram textureShader = new ShaderProgram("/shaders/texturesModel/vertex.glsl", "/shaders/texturesModel/fragment.glsl");
    public final ShaderProgram items2DShader = new ShaderProgram("/shaders/2dItemModels/vertex.glsl", "/shaders/2dItemModels/fragment.glsl");
    public final ShaderProgram breakingStagesShader = new ShaderProgram("/shaders/texturesModel/vertex.glsl", "/shaders/breakingStages/fragment.glsl");
    public final TextureAtlas blockTextureAtlas = new TextureAtlas(
            BlockType.getAllTexturesPaths().toArray(String[]::new)
    );
}
