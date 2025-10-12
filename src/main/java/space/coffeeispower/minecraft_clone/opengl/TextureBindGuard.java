package space.coffeeispower.minecraft_clone.opengl;

import space.coffeeispower.minecraft_clone.resources.InfallibleAutoClose;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL13.*;

public final class TextureBindGuard implements InfallibleAutoClose {
    private final int oldId;
    private final int newId;
    private final byte oldSlot;
    private final byte newSlot;
    public TextureBindGuard(int newId, byte newSlot) {
        this.newSlot = newSlot;
        this.newId = newId;

        int[] old = new int[1];
        glGetIntegerv(GL_ACTIVE_TEXTURE, old);
        oldSlot = (byte) old[0];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, old);
        this.oldId = old[0];

        if (oldId != newId || oldSlot != newSlot)
        {
            glActiveTexture(GL_TEXTURE0 + newSlot);
            glBindTexture(GL_TEXTURE_2D, newId);
        }
    }

    @Override
    public void close() {
        if (oldId != newId || oldSlot != newSlot)
        {
            glActiveTexture(GL_TEXTURE0 + oldSlot);
            glBindTexture(GL_TEXTURE_2D, oldId);
        }
    }
}
