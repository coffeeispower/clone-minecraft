package space.coffeeispower;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import space.coffeeispower.window.Window;

import java.util.Objects;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.*;
/**
 * Contem a lógica principal do jogo, ao ser construida, inicializa o jogo e ao ser destruído,
 * libera os recursos.
 * */
public class Game implements AutoCloseable {
    private Window window;

    public Game(
        // Isto precisa de ser um valor criado de maneira preguiçosa, porque apenas é possível criar janelas
        // após inicializar o GLFW
        Supplier<Window> createWindow
    ) {
        GLFWErrorCallback.createPrint(System.err).set();
        if ( !GLFW.glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");
        this.window = Objects.requireNonNull(createWindow.get());
        GL.createCapabilities();
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
            glViewport(0, 0, window.width(), window.height()); // Ter a certeza que o OpenGL está sincronizado com o tamanho da janela
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpar o ultimo frame
            glClearColor(0.1f, 0.1f, 0.1f, 1); // Preencher a janela com cinzento
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
