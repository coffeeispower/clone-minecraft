package space.coffeeispower.opengl;

import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.glGetIntegerv;

public final class BindGuard implements AutoCloseable {
    private final int oldId;
    private final int newId;
    private final Consumer<Integer> bind;
    public BindGuard(int newId, Consumer<Integer> bind, int pname) {
        int[] old = new int[1];
        glGetIntegerv(pname, old);
        this.oldId = old[0];
        this.newId = newId;
        this.bind = bind;
        if (oldId != newId)
            bind.accept(newId);
    }

    @Override
    public void close() {
        if (oldId != newId)
            this.bind.accept(oldId);
    }
}
