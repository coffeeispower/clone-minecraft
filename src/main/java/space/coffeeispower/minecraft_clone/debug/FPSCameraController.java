package space.coffeeispower.minecraft_clone.debug;

import org.joml.Vector3d;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.window.Window;

import static org.lwjgl.glfw.GLFW.*;

public final class FPSCameraController {
    private static final float SENSITIVITY = 0.002f;
    private static final float SPEED = 10.0f;
    private static final float FAST_SPEED = 40.0f;
    private final Window window;
    private final Camera camera;
    private boolean enabled = false;
    private double lastMouseX, lastMouseY;

    public FPSCameraController(Window window, Camera camera) {
        this.window = window;
        this.camera = camera;
        double[] pos = window.getCursorPos();
        lastMouseX = pos[0];
        lastMouseY = pos[1];
        enable();
    }

    public void enable() {
        enabled = true;
        window.setGrab(true);
    }

    public void disable() {
        enabled = false;
        window.setGrab(false);
    }

    public void update(double deltaTime) {
        if (!enabled) return;

        updateMouse();
        updateKeyboard(deltaTime);
    }

    private void updateMouse() {
        double[] pos = window.getCursorPos();
        double dx = pos[0] - lastMouseX;
        double dy = pos[1] - lastMouseY;

        lastMouseX = pos[0];
        lastMouseY = pos[1];

        Vector3d rotation = camera.rotation();
        rotation.x += -dy * SENSITIVITY;
        rotation.y += -dx * SENSITIVITY;
        rotation.x = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, rotation.x));
    }

    private void updateKeyboard(double deltaTime) {
        Vector3d position = camera.position();
        Vector3d forward = getForward();
        Vector3d right = getRight();
        float velocity = (float) ((window.isKeyPressed(GLFW_KEY_LEFT_CONTROL) ? FAST_SPEED : SPEED) * deltaTime);

        if (window.isKeyPressed(GLFW_KEY_W))
            position.add(new Vector3d(forward).mul(velocity));
        if (window.isKeyPressed(GLFW_KEY_S))
            position.sub(new Vector3d(forward).mul(velocity));
        if (window.isKeyPressed(GLFW_KEY_D))
            position.add(new Vector3d(right).mul(velocity));
        if (window.isKeyPressed(GLFW_KEY_A))
            position.sub(new Vector3d(right).mul(velocity));

        if (window.isKeyPressed(GLFW_KEY_SPACE))
            position.add(new Vector3d(0, velocity, 0));

        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT))
            position.add(new Vector3d(0, -velocity, 0));
//        System.out.print("X: ");
//        System.out.print(position.x);
//        System.out.print(" Y: ");
//        System.out.print(position.y);
//        System.out.print(" Z: ");
//        System.out.print(position.z);
//        System.out.print("\r");
    }

    private Vector3d getForward() {
        Vector3d rot = camera.rotation();
        double cosPitch = Math.cos(-rot.x);
        return new Vector3d(
                Math.sin(-rot.y) * cosPitch,
                0,
                -Math.cos(-rot.y) * cosPitch
        ).normalize();
    }

    private Vector3d getRight() {
        Vector3d rot = camera.rotation();
        return new Vector3d(
                Math.cos(-rot.y),
                0,
                Math.sin(-rot.y)
        ).normalize();
    }

    public Camera camera() {
        return camera;
    }

}
