package space.coffeeispower;

import org.joml.Matrix4d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import space.coffeeispower.opengl.Camera;
import space.coffeeispower.opengl.Draw;
import space.coffeeispower.opengl.ShaderProgram;
import space.coffeeispower.opengl.model.BufferGroup;
import space.coffeeispower.opengl.texture.TextureAtlas;
import space.coffeeispower.window.Window;
import space.coffeeispower.world.BlockType;
import space.coffeeispower.world.Chunk;
import space.coffeeispower.world.ChunkMeshGenerator;
import space.coffeeispower.world.WorldGen;

import java.util.Objects;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL33.*;
/**
 * Contem a lógica principal do jogo, ao ser construida, inicializa o jogo e ao ser destruído,
 * libera os recursos.
 * */
public final class Game implements AutoCloseable {
    private Window window;
    private final BufferGroup bufferGroup;
    private final ShaderProgram defaultShader;
    private final FPSCameraController fpsCamera;
    private final TextureAtlas blockTextureAtlas;
    private double lastTimeSec = System.currentTimeMillis()/1000.;
    public Game(
        // Isto precisa de ser um valor criado de maneira preguiçosa, porque apenas é possível criar janelas
        // após inicializar o GLFW
        Supplier<Window> createWindow
    ) throws Exception {
        GLFWErrorCallback.createPrint(System.err).set();
        if ( !GLFW.glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");
        this.window = Objects.requireNonNull(createWindow.get());
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        this.defaultShader = new ShaderProgram("/vertexshader.glsl", "/fragmentshader.glsl");

        blockTextureAtlas = new TextureAtlas(BlockType.getAllTexturesPaths().toArray(new String[0]));
        Chunk chunk = new Chunk(0, 0);
        WorldGen.generateChunk(chunk);
        this.bufferGroup = ChunkMeshGenerator.generateMeshForChunk(chunk, blockTextureAtlas);

        fpsCamera = new FPSCameraController(window, new Camera(new Camera.Perspective(70)));
    }
    /**
     * Retorna a janela principal controlada pelo jogo
     * */
    public Window window() {
        Objects.requireNonNull(window);
        return window;
    }
    /**
     * Inicia o loop de renderização do jogo, esta função bloqueia até o jogador fechar a janela.
     * */
    public void loop() {
        Objects.requireNonNull(window);
        while(!window.shouldClose()) {
            var now = System.currentTimeMillis()/1000.;
            var deltaTime = now - lastTimeSec;
            lastTimeSec = now;
            fpsCamera.update(deltaTime);
            glViewport(0, 0, window.width(), window.height()); // Ter a certeza que o OpenGL está sincronizado com o tamanho da janela
            glClearColor(0.1f, 0.1f, 0.1f, 1); // Preencher a janela com cinzento
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpar o ultimo frame
            try (var ignored = blockTextureAtlas.texture().bind(0)) {
                defaultShader.setUniform("textureAtlas", 0);
                Draw.triangles(window, bufferGroup, defaultShader, fpsCamera.camera(), new Matrix4d());
            }
            window.swapBuffers(); // Enviar tudo o que foi desenhado para a janela e para o ecrã
            GLFW.glfwPollEvents(); // Ler teclado e rato e outros inputs

        }
    }

    @Override
    public void close() {
        Objects.requireNonNull(window);
        window.close();
        window = null;
    }
}
