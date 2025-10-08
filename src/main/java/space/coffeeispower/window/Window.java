package space.coffeeispower.window;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

/**
 * <p>Representa uma janela no sistema operativo</p>
 * <p>Ao ser criado um objeto desta classe, uma nova janela é criada no sistema operativo com um contexto OpenGL.</p>
 */
public final class Window implements AutoCloseable {
    long id;

    public Window() {
        glfwDefaultWindowHints();
        id = glfwCreateWindow(800, 600, "Clone Minecraft - Projeto de Programação (Tiago e Daniel Londoño)", 0, 0);
        if (id == 0) throw new RuntimeException("Falha ao criar janela");
        glfwMakeContextCurrent(id); // Trazer o contexto da janela para a thread atual
        glfwSwapInterval(1); // Ativar V-Sync
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

    public boolean isKeyPressed(int key) {
        return glfwGetKey(id, key) == GLFW_PRESS;
    }
    public void setGrab(boolean grabbed) {
        glfwSetInputMode(id, GLFW_CURSOR, grabbed ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    }
    public double[] getCursorPos() {
        double[] posX = new double[1];
        double[] posY = new double[1];
        glfwGetCursorPos(id, posX, posY);
        return new double[]{posX[0], posY[0]};
    }
}
