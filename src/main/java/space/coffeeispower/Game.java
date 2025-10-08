package space.coffeeispower;

import org.joml.Matrix4d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import space.coffeeispower.opengl.Camera;
import space.coffeeispower.opengl.Draw;
import space.coffeeispower.opengl.ShaderProgram;
import space.coffeeispower.opengl.Texture;
import space.coffeeispower.opengl.model.Buffer;
import space.coffeeispower.opengl.model.BufferGroup;
import space.coffeeispower.window.Window;

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
    private double angle;
    private final FPSCameraController fpsCamera;
    private final Texture testTexture;
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

        //////////// modelos e shaders de teste

        this.bufferGroup = new BufferGroup();
        // posições
        bufferGroup.addBuffer(new Buffer(new double[]{
                // frente
                -0.5, -0.5,  0.5,
                0.5, -0.5,  0.5,
                0.5,  0.5,  0.5,
                0.5,  0.5,  0.5,
                -0.5,  0.5,  0.5,
                -0.5, -0.5,  0.5,

                // trás
                -0.5, -0.5, -0.5,
                -0.5,  0.5, -0.5,
                0.5,  0.5, -0.5,
                0.5,  0.5, -0.5,
                0.5, -0.5, -0.5,
                -0.5, -0.5, -0.5,

                // esquerda
                -0.5,  0.5,  0.5,
                -0.5,  0.5, -0.5,
                -0.5, -0.5, -0.5,
                -0.5, -0.5, -0.5,
                -0.5, -0.5,  0.5,
                -0.5,  0.5,  0.5,

                // direita
                0.5,  0.5,  0.5,
                0.5, -0.5, -0.5,
                0.5,  0.5, -0.5,
                0.5, -0.5, -0.5,
                0.5,  0.5,  0.5,
                0.5, -0.5,  0.5,

                // topo
                -0.5,  0.5, -0.5,
                -0.5,  0.5,  0.5,
                0.5,  0.5,  0.5,
                0.5,  0.5,  0.5,
                0.5,  0.5, -0.5,
                -0.5,  0.5, -0.5,

                // fundo
                -0.5, -0.5, -0.5,
                0.5, -0.5, -0.5,
                0.5, -0.5,  0.5,
                0.5, -0.5,  0.5,
                -0.5, -0.5,  0.5,
                -0.5, -0.5, -0.5
        }, 3));

        // coordenadas de textura (36 vértices × 2 componentes)
        bufferGroup.addBuffer(new Buffer(new double[]{
                // frente
                0,0, 1,0, 1,1,
                1,1, 0,1, 0,0,

                // trás
                0,0, 0,1, 1,1,
                1,1, 1,0, 0,0,

                // esquerda
                1,1, 1,0, 0,0,
                0,0, 0,1, 1,1,

                // direita
                1,1, 0,0, 1,0,
                0,0, 1,1, 0,1,

                // topo
                0,1, 0,0, 1,0,
                1,0, 1,1, 0,1,

                // fundo
                0,1, 1,1, 1,0,
                1,0, 0,0, 0,1
        }, 2));

        this.defaultShader = new ShaderProgram("/vertexshader.glsl", "/fragmentshader.glsl");
        fpsCamera = new FPSCameraController(window, new Camera(new Camera.Perspective(70)));
        testTexture = new Texture("/testtexture.png");
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
            try (var ignored = testTexture.bind(0)) {
                defaultShader.setUniform("testTexture", 0);
                Draw.triangles(window, bufferGroup, defaultShader, fpsCamera.camera(), new Matrix4d().translate(0, -1, -3).rotateY(Math.toRadians(angle)));
            }
            angle++;
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
