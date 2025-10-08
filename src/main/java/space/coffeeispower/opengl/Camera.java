package space.coffeeispower.opengl;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import space.coffeeispower.window.Window;

public record Camera(Vector3d position, Vector3d rotation, Mode mode) {
    public static final Camera DEFAULT_CAMERA = new Camera(new Vector3d(), new Vector3d(), new Camera.Perspective(70));

    public Matrix4d toViewMatrix() {
        return new Matrix4d().translate(new Vector3d(position).negate()).rotateXYZ(rotation);
    }

    public Matrix4d toProjectionMatrix(Window window) {
        return switch (mode) {
            case Perspective perspective -> new Matrix4d().perspective(
                    perspective.fov,
                    (double) window.width() / (double) window.height(),
                    0.001,
                    1000
            );
            case Orthogonal ignored -> new Matrix4d().ortho2D(0, window.width(), window.height(), 0);
        };
    }

    public sealed interface Mode {}

    public record Perspective(double fov) implements Mode {}

    public record Orthogonal() implements Mode {}
}
