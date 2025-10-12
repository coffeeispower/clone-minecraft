package space.coffeeispower.minecraft_clone.item.view;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.resources.InfallibleAutoClose;
import space.coffeeispower.minecraft_clone.window.Window;

public interface ItemModel extends InfallibleAutoClose {

    default void renderInThirdPersonView(Matrix4f transform) {
        throw new UnsupportedOperationException("TODO: renderInThirdPersonView is not implemented yet");
    }

    void renderAsUi(Matrix4d transform);

    void renderInFirstPersonView(Matrix4d transform, Camera.Perspective perspective, Window window);
}
