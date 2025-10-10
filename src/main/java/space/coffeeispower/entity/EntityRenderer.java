package space.coffeeispower.entity;

import space.coffeeispower.opengl.Camera;
import space.coffeeispower.window.Window;

public interface EntityRenderer<T extends Entity<T>> {
    T getEntity();
    void render(Window window, Camera camera);

}
