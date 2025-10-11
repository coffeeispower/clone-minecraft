package space.coffeeispower.minecraft_clone.entity;

import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.window.Window;

public interface EntityRenderer<T extends Entity<T>> {
    T getEntity();
    void render(Window window, Camera camera);

}
