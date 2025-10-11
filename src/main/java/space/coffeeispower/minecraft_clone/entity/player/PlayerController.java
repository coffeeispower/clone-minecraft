package space.coffeeispower.minecraft_clone.entity.player;

import org.joml.Vector2d;
import space.coffeeispower.minecraft_clone.raycast.WorldRaycaster;
import space.coffeeispower.minecraft_clone.window.Window;

import static org.lwjgl.glfw.GLFW.*;

public class PlayerController {

    private final Player player;
    private final Window window;
    private double lastMouseX, lastMouseY;
    private static final float SENSITIVITY = 0.002f;
    private WorldRaycaster.BlockRaycastResult hoveredBlock;
    public PlayerController(Player player, Window window) {
        this.player = player;
        this.window = window;
        window.setGrab(true);
    }

    public Player getPlayer() {
        return player;
    }
    public void update(double deltaTime) {
        if(!window.isGrabbed()) return;
        var newHoveredBlock = getPlayer().raycast(3.6);
        var world = player.getWorld();
        if (!window.isMouseDown(0)) {
            if (hoveredBlock != null && world.isBlockBeingBroken(hoveredBlock.blockPosition())) {
                world.interruptBlockBreaking(hoveredBlock.blockPosition());
            }
        } else if (newHoveredBlock != null) {
            if (hoveredBlock != null && !newHoveredBlock.blockPosition().equals(hoveredBlock.blockPosition()) && world.isBlockBeingBroken(hoveredBlock.blockPosition())) {
                world.interruptBlockBreaking(hoveredBlock.blockPosition());
                world.startBreakingBlock(player, newHoveredBlock.blockPosition());
            } else if (!world.isBlockBeingBroken(newHoveredBlock.blockPosition())) {
                world.startBreakingBlock(player, newHoveredBlock.blockPosition());
            }
        } else if (hoveredBlock != null) {
            world.interruptBlockBreaking(hoveredBlock.blockPosition());
        }
        hoveredBlock = newHoveredBlock;
        handleWalking(deltaTime);
        handleMouseTurning();
    }

    private void handleWalking(double deltaTime) {
        if (window.isKeyPressed(GLFW_KEY_SPACE) /*&& player.canJump()*/)
            player.getMotion().add(0, 8, 0).mul(1.01, 1.0, 1.01);
        var direction = new Vector2d();
        if (window.isKeyPressed(GLFW_KEY_W))
            direction.y--;
        if (window.isKeyPressed(GLFW_KEY_S))
            direction.y++;
        if (window.isKeyPressed(GLFW_KEY_A))
            direction.x--;
        if (window.isKeyPressed(GLFW_KEY_D))
            direction.x++;

        player.walk(direction, deltaTime);

    }

    private void handleMouseTurning() {
        double[] pos = window.getCursorPos();
        double dx = pos[0] - lastMouseX;
        double dy = pos[1] - lastMouseY;

        lastMouseX = pos[0];
        lastMouseY = pos[1];

        Vector2d rotation = player.getRotation();
        rotation.x += -dy * SENSITIVITY;
        rotation.y += -dx * SENSITIVITY;
        rotation.x = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, rotation.x));
    }

    public WorldRaycaster.BlockRaycastResult getHoveredBlock() {
        return hoveredBlock;
    }
}
