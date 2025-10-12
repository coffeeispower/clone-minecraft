package space.coffeeispower.minecraft_clone.opengl.texture.t2d;

import org.joml.Vector2i;
import space.coffeeispower.minecraft_clone.opengl.texture.Texture;

public interface Texture2D extends Texture {
    default Vector2i getSize() {
        return new Vector2i(getWidth(), getHeight());
    }

    int getWidth();

    int getHeight();
}
