package space.coffeeispower.opengl;

import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.glGetIntegerv;

public class BindGuard implements AutoCloseable {
    private final int oldId;
    private final Consumer<Integer> bind;
    public BindGuard(int newId, Consumer<Integer> bind, int pname) {
        int[] old = new int[1];
        glGetIntegerv(pname, old);
        bind.accept(newId);
        this.oldId = old[0];
        this.bind = bind;
    }

    @Override
    public void close() {
        this.bind.accept(oldId);
    }
}
