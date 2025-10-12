package space.coffeeispower.minecraft_clone.opengl.texture;

import org.lwjgl.opengl.GL33;
import space.coffeeispower.minecraft_clone.resources.InfallibleAutoClose;
import space.coffeeispower.minecraft_clone.opengl.TextureBindGuard;

public interface Texture extends AutoCloseable {
    int getTextureId();

    default InfallibleAutoClose bindTextureOnSlot(int slot) {
        return new TextureBindGuard(getTextureId(), (byte) slot);
    }

    @Override
    default void close() {
        GL33.glDeleteTextures(getTextureId());
    }

}
