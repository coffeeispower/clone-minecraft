package space.coffeeispower.minecraft_clone;

import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import space.coffeeispower.minecraft_clone.entity.player.Player;
import space.coffeeispower.minecraft_clone.entity.player.PlayerController;
import space.coffeeispower.minecraft_clone.item.ItemStack;
import space.coffeeispower.minecraft_clone.item.ItemType;
import space.coffeeispower.minecraft_clone.item.view.ItemModelRegistry;
import space.coffeeispower.minecraft_clone.resources.InfallibleAutoClose;
import space.coffeeispower.minecraft_clone.resources.Resources;
import space.coffeeispower.minecraft_clone.ui.Crosshair;
import space.coffeeispower.minecraft_clone.window.Window;
import space.coffeeispower.minecraft_clone.world.World;
import space.coffeeispower.minecraft_clone.world.WorldRenderer;

import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL33.*;
/**
 * Contem a lógica principal do jogo, ao ser construida, inicializa o jogo e ao ser destruído,
 * libera os recursos.
 * */
public final class Game implements InfallibleAutoClose {
    private Window window;
    private double lastTimeSec = System.currentTimeMillis()/1000.;
    private final World world = new World(new Random().nextInt());
    private final WorldRenderer worldRenderer;
    private final PlayerController thePlayer;
    private final ItemModelRegistry itemModelRegistry;
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
        thePlayer = new PlayerController(world.spawnEntity(new Player(new Vector3d(0, 100, 0), world)), window);
        thePlayer.getPlayer().getInventory().add(new ItemStack(ItemType.Dirt, (short) 64));
        thePlayer.getPlayer().getInventory().add(new ItemStack(ItemType.Bedrock, (short) 64));
        itemModelRegistry = new ItemModelRegistry();
        window.onKeyPress(GLFW.GLFW_KEY_F8, world::unloadAllChunks);
    }
    /**
     * Inicia o loop de renderização do jogo, esta função bloqueia até o jogador fechar a janela.
     * */
    public void loop() {
        Objects.requireNonNull(window);
        while(!window.shouldClose()) {
            GLFW.glfwPollEvents(); // Ler teclado e rato e outros inputs
            var now = System.currentTimeMillis()/1000.;
            var deltaTime = now - lastTimeSec;
            lastTimeSec = now;
            thePlayer.update(deltaTime, window);
            var camera = thePlayer.getPlayer().getEye();

            world.garbageCollectChunks(camera.position().x(), camera.position().z());
            world.loadChunksAround(camera.position().x(), camera.position().z());
            world.updateEntities(deltaTime);
            world.updateBreakingStates(deltaTime);


            glViewport(0, 0, window.width(), window.height()); // Ter a certeza que o OpenGL está sincronizado com o tamanho da janela
            glClearColor(5f / 255, 180f / 255, 240f / 255, 1); // Preencher a janela com cinzento
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpar o ultimo frame
            worldRenderer.renderWorld(camera);
            worldRenderer.renderEntities(window, camera);
            worldRenderer.renderBlockHighlight(thePlayer, camera);
            worldRenderer.renderBlockBreaking(thePlayer, camera);
            thePlayer.renderFirstPersonView(itemModelRegistry, window);
            Crosshair.renderCrosshair();

            window.swapBuffers(); // Enviar tudo o que foi desenhado para a janela e para o ecrã
        }
    }

    @Override
    public void close() {
        worldRenderer.close();
        window.close();
        window = null;
    }
}
