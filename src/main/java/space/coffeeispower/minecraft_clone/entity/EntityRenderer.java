package space.coffeeispower.minecraft_clone.entity;

import space.coffeeispower.minecraft_clone.opengl.Camera;

public interface EntityRenderer<T extends Entity<T>> {
    T getEntity();

    void render(Camera camera);
}
