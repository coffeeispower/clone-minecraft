package space.coffeeispower.window;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Representa uma janela no sistema operativo
 * Ao ser criado um objeto desta classe, uma nova janela é criada no sistema operativo com um contexto OpenGL.
 */
public class Window implements AutoCloseable {
    long id;

    public Window() {
        glfwDefaultWindowHints();
        id = glfwCreateWindow(
                800, 600,
                "Clone Minecraft - Projeto de Programação (Tiago e Daniel Londoño)",
                0, 0
        );
        if (id == 0)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwMakeContextCurrent(id);
        glfwSwapInterval(1);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(id);
    }

    public void swapBuffers() {
        glfwSwapBuffers(id);
    }

    public int width() {
        int[] width = {0};
        glfwGetWindowSize(id, width, null);
        return width[0];
    }

    public int height() {
        int[] height = {0};
        glfwGetWindowSize(id, null, height);
        return height[0];
    }

    @Override
    public void close() {
        glfwFreeCallbacks(id);
        glfwDestroyWindow(id);
    }
}
