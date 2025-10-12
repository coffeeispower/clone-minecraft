package space.coffeeispower.minecraft_clone.opengl;

import org.joml.Matrix4d;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glGetIntegerv;

public final class Camera {
    public static final Camera DEFAULT_CAMERA = new Camera(new Vector3d(), new Vector3d(), new Perspective(90));
    private Vector3d position;
    private Vector3d rotation;
    private Mode mode;
    public static final Camera DEFAULT_UI_CAMERA = new Camera(new UI());
    public Camera(Vector3d position, Vector3d rotation, Mode mode) {
        this.position = position;
        this.rotation = rotation;
        this.mode = mode;
    }
    public Camera(Mode mode) {
        this(new Vector3d(), new Vector3d(), mode);
    }

    public Camera() {
        this(new Vector3d(), new Vector3d(), null);
    }
    public Camera(Vector3d position, Mode mode) {
        this(position, new Vector3d(), mode);
    }
    public Matrix4d toViewMatrix() {
        return new Matrix4d().rotateXYZ(new Vector3d(rotation).negate()).translate(new Vector3d(position).negate());
    }

    public Matrix4d toProjectionMatrix() {
        Vector2i screenSize = new Vector2i();
        // Guardar estado atual
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer prev = stack.callocInt(4);
            glGetIntegerv(GL_VIEWPORT, prev);
            screenSize.x = prev.get(2);
            screenSize.y = prev.get(3);
        }
        var aspectRatio = (double) screenSize.x / (double) screenSize.y;
        return switch (mode) {
            case null -> new Matrix4d();
            case Perspective perspective -> new Matrix4d().perspective(
                    Math.toRadians(perspective.fov),
                    aspectRatio,
                    0.01,
                    16*20
            );
            case UI ignored -> //noinspection IntegerDivisionInFloatingPointContext
                    new Matrix4d().ortho2D(-screenSize.x / 2, screenSize.x / 2, -screenSize.y / 2, screenSize.y / 2);
            case Orthogonal ignored -> //noinspection IntegerDivisionInFloatingPointContext
                    new Matrix4d().ortho(-1, 1, -(1 / aspectRatio), (1 / aspectRatio), -100, 100);

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

    public record UI() implements Mode {
    }
    public record Orthogonal() implements Mode {
    }


}
