package space.coffeeispower;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import space.coffeeispower.window.Window;

import java.util.Objects;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.*;

public class Game implements AutoCloseable {
    private Window window;
    public Game(Supplier<Window> createWindow) {
        GLFWErrorCallback.createPrint(System.err).set();
        if ( !GLFW.glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");
        this.window = Objects.requireNonNull(createWindow.get());
        GL.createCapabilities();
    }

    public Window window() {
        Objects.requireNonNull(window);
        return window;
    }
    public void loop() {
        Objects.requireNonNull(window);
        while(!window.shouldClose()) {
            glViewport(0, 0, window.width(), window.height()); // Ter a certeza que o opengl está sincronizado com o tamanho da janela
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
        GLFW.glfwTerminate();
    }
}
