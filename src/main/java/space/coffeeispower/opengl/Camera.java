package space.coffeeispower.opengl;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import space.coffeeispower.window.Window;

public final class Camera {
    public static final Camera DEFAULT_CAMERA = new Camera(new Vector3d(), new Vector3d(), new Perspective(70));
    private Vector3d position;
    private Vector3d rotation;
    private Mode mode;

    public Camera(Vector3d position, Vector3d rotation, Mode mode) {
        this.position = position;
        this.rotation = rotation;
        this.mode = mode;
    }

    public Matrix4d toViewMatrix() {
        return new Matrix4d().translate(new Vector3d(position).negate()).rotateXYZ(new Vector3d(rotation).negate());
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

    public Vector3d position() {
        return position;
    }

    public Vector3d rotation() {
        return rotation;
    }

    public Mode mode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }

    public void setRotation(Vector3d rotation) {
        this.rotation = rotation;
    }


    public sealed interface Mode {
    }

    public record Perspective(double fov) implements Mode {
    }

    public record Orthogonal() implements Mode {
    }


}
