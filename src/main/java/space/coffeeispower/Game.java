package space.coffeeispower;

import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import space.coffeeispower.entity.player.Player;
import space.coffeeispower.entity.player.PlayerController;
import space.coffeeispower.models.Resources;
import space.coffeeispower.window.Window;
import space.coffeeispower.world.World;
import space.coffeeispower.world.WorldRenderer;

import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL33.*;
/**
 * Contem a lógica principal do jogo, ao ser construida, inicializa o jogo e ao ser destruído,
 * libera os recursos.
 * */
public final class Game implements AutoCloseable {
    private Window window;
//    private final FPSCameraController fpsCamera;
    private double lastTimeSec = System.currentTimeMillis()/1000.;
    private final World world = new World(new Random().nextInt());
    private final WorldRenderer worldRenderer;
    private final PlayerController thePlayer;
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
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        Resources.init();
        worldRenderer = new WorldRenderer(world);
//        fpsCamera = new FPSCameraController(window, new Camera(new Vector3d(0, 20, 0), new Camera.Perspective(70)));
//        window.onKeyPress(GLFW.GLFW_KEY_KP_5, () -> world.spawnEntity(new TestEntity(new Vector3d(0, 30, -20), new Vector4f(1, 1, 1, 1), world)));
//        window.onKeyPress(GLFW.GLFW_KEY_KP_7, () -> world.pushAllEntities(new Vector3d(0, 5, 0)));
//        window.onKeyPress(GLFW.GLFW_KEY_KP_8, () -> world.pushAllEntities(new Vector3d(0, 0, -5)));
//        window.onKeyPress(GLFW.GLFW_KEY_KP_4, () -> world.pushAllEntities(new Vector3d(-5, 0, 0)));
//        window.onKeyPress(GLFW.GLFW_KEY_KP_6, () -> world.pushAllEntities(new Vector3d(5, 0, 0)));
//        window.onKeyPress(GLFW.GLFW_KEY_KP_2, () -> world.pushAllEntities(new Vector3d(0, 0, 5)));
        thePlayer = new PlayerController(world.spawnEntity(new Player(new Vector3d(0, 100, 0), world)), window);

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
            thePlayer.update(deltaTime);
//            fpsCamera.update(deltaTime);
            var camera = thePlayer.getPlayer().getEye();

            world.garbageCollectChunks(camera.position().x(), camera.position().z());
            world.loadChunksAround(camera.position().x(), camera.position().z(), worldRenderer.getBlockTextureAtlas());
            world.updateEntities(deltaTime);


            glViewport(0, 0, window.width(), window.height()); // Ter a certeza que o OpenGL está sincronizado com o tamanho da janela
            glClearColor(0.1f, 0.1f, 0.1f, 1); // Preencher a janela com cinzento
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpar o ultimo frame
            worldRenderer.renderWorld(window, camera);
            var raycast = thePlayer.getPlayer().raycast(3.6);
            if(raycast != null) {
                worldRenderer.renderBlockHighlight(raycast.blockPosition(), window, camera);
            }
            worldRenderer.renderEntities(window, camera);
            window.swapBuffers(); // Enviar tudo o que foi desenhado para a janela e para o ecrã
            GLFW.glfwPollEvents(); // Ler teclado e rato e outros inputs

        }
    }

    @Override
    public void close() {
        worldRenderer.close();
        window.close();
        window = null;
    }
}
