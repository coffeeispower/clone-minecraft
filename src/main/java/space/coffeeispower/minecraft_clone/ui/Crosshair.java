package space.coffeeispower.minecraft_clone.ui;

import space.coffeeispower.minecraft_clone.opengl.Draw;
import space.coffeeispower.minecraft_clone.resources.Resources;

public class Crosshair {
    public static void renderCrosshair() {
        Draw.textureAtCenter(Resources.INSTANCE.crosshairTexture);
    }
}
