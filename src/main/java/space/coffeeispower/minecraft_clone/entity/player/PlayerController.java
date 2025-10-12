package space.coffeeispower.minecraft_clone.entity.player;

import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.joml.Vector3i;
import space.coffeeispower.minecraft_clone.item.view.ItemModelRegistry;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.raycast.WorldRaycaster;
import space.coffeeispower.minecraft_clone.window.Window;
import space.coffeeispower.minecraft_clone.world.block.BlockType;

import static org.lwjgl.glfw.GLFW.*;

public class PlayerController {

    private static final float SENSITIVITY = 0.002f;
    private static final double SWING_DURATION = 0.24; // segundos

    private final Player player;
    private final Window window;
    private double lastMouseX, lastMouseY;
    private WorldRaycaster.BlockRaycastResult hoveredBlock;

    // 🎬 Swing animation
    private boolean isSwinging = false;
    private double swingTimer = 0.0;
    private boolean lastLeftClickDown;
    private boolean lastRightClickDown;
    public PlayerController(Player player, Window window) {
        this.player = player;
        this.window = window;
        window.setGrab(true);
    }

    public Player getPlayer() {
        return player;
    }

    public void update(double deltaTime) {
        if (!window.isGrabbed()) return;

        hoveredBlock = getPlayer().raycast(3.6);
        var justClicked = !lastLeftClickDown && window.isMouseDown(0);
        var justRightClicked = !lastRightClickDown && window.isMouseDown(1);
        lastLeftClickDown = window.isMouseDown(0);
        lastRightClickDown = window.isMouseDown(1);
        if (justClicked) {
            triggerSwing(true);
        }
        // Clique esquerdo = quebrar bloco
        if (window.isMouseDown(0)) {
            if (hoveredBlock != null && !player.isBreakingBlock(hoveredBlock.blockPosition())) {
                player.startBreakingBlock(hoveredBlock.blockPosition());
            } else if (hoveredBlock == null) {
                player.stopBreakingBlock();
            }
        } else {
            player.stopBreakingBlock();
        }
        if (hoveredBlock != null && player.isBreakingBlock(hoveredBlock.blockPosition())) {
            triggerSwing(false);
        }
        var itemInHand = player.getItemInHand();
        var itemInHandAssociatedBlock = itemInHand == null ? null : itemInHand.type().getAssociatedBlock();
        if (justRightClicked && hoveredBlock != null && itemInHand != null && itemInHandAssociatedBlock != null) {
            triggerSwing(true);
            var world = player.getWorld();
            var hitFace = hoveredBlock.getHitFace();
            world.setBlock(hoveredBlock.blockPosition().x + hitFace.getOffsetX(), hoveredBlock.blockPosition().y + hitFace.getOffsetY(), hoveredBlock.blockPosition().z + hitFace.getOffsetZ(), itemInHandAssociatedBlock);
            player.decreaseItemInHand((short) 1);
        }
        // Atualizar o swing
        updateSwing(deltaTime);

        handleWalking(deltaTime);
        handleMouseTurning();
    }

    // ==========================
    // 🎬 Swing animation control
    // ==========================

    public void triggerSwing(boolean allowEarlyReswing) {
        if (isSwinging && (!allowEarlyReswing || swingTimer < 0.18)) return; // já está a decorrer
        swingTimer = 0;
        isSwinging = true;
    }

    private void updateSwing(double deltaTime) {
        if (!isSwinging) return;

        swingTimer += deltaTime;
        if (swingTimer >= SWING_DURATION) {
            isSwinging = false;
            swingTimer = 0.0;
        }
    }

    public double getSwingProgress() {
        if (!isSwinging) return 0.0;
        return swingTimer / SWING_DURATION;
    }

    public boolean isSwinging() {
        return isSwinging;
    }


    public BreakingBlock getBreakingBlock() {
        if (hoveredBlock == null || !player.isBreakingBlock(hoveredBlock.blockPosition())) {
            return null;
        }
        return new BreakingBlock(player.getWorld().getBlock(hoveredBlock.blockPosition()),
                hoveredBlock.blockPosition(),
                player.getWorld().getBreakingProgress(hoveredBlock.blockPosition()));
    }

    private void handleWalking(double deltaTime) {
        if (window.isKeyPressed(GLFW_KEY_SPACE) && player.canJump())
            player.getMotion().add(0, 8, 0).mul(1.01, 1.0, 1.01);

        var direction = new Vector2d();
        if (window.isKeyPressed(GLFW_KEY_W)) direction.y--;
        if (window.isKeyPressed(GLFW_KEY_S)) direction.y++;
        if (window.isKeyPressed(GLFW_KEY_A)) direction.x--;
        if (window.isKeyPressed(GLFW_KEY_D)) direction.x++;

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

    public record BreakingBlock(BlockType block, Vector3i position, double progress) {
    }

    public void renderFirstPersonView(ItemModelRegistry itemModelRegistry, Window window) {
        var itemInHand = player.getItemInHand();
        if (itemInHand == null) return;

        double swing = getSwingProgress();
        Matrix4d transform = new Matrix4d();

        if (swing > 0) {
            // Curva suavizada (ease in-out sinusoidal)
            double swingSin = Math.sin(swing * Math.PI);
            double negativeSwingSin = Math.sin((1 - swing * 2) * Math.PI);

            // Movimento do item (em blocos)
            double offsetX = -0.5 * swingSin;  // move um pouco para a esquerda
            double offsetY = 0.5 * negativeSwingSin;  // desce ligeiramente
            double offsetZ = -1.0 * swingSin;  // vai um pouco para trás

            // Aplicar a translação do swing
            transform.translate(offsetX, offsetY, offsetZ);

            // Rotação suave (como o braço a balançar)
            double rotX = -Math.toRadians(40.0 * swingSin);
            double rotY = Math.toRadians(10.0 * swingSin);
            double rotZ = Math.toRadians(20.0 * swingSin * 2);

            transform.rotateXYZ(rotX, rotY, rotZ);
        }

        itemModelRegistry.renderInFirstPersonView(
                itemInHand.type(),
                transform,
                new Camera.Perspective(70),
                window
        );
    }

}
