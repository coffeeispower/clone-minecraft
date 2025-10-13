package space.coffeeispower.minecraft_clone.entity.player;

import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.joml.Vector3i;
import space.coffeeispower.minecraft_clone.entity.player.animation.ItemSwapAnimationController;
import space.coffeeispower.minecraft_clone.item.ItemStack;
import space.coffeeispower.minecraft_clone.item.view.ItemModelRegistry;
import space.coffeeispower.minecraft_clone.math.AABBd;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.raycast.WorldRaycaster;
import space.coffeeispower.minecraft_clone.ui.HotbarUI;
import space.coffeeispower.minecraft_clone.ui.PlayerInventoryUI;
import space.coffeeispower.minecraft_clone.window.Window;
import space.coffeeispower.minecraft_clone.world.block.BlockType;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public class PlayerController {

    private static final float SENSITIVITY = 0.002f;
    private static final double SWING_DURATION = 0.26; // segundos

    private final Player player;
    private final Window window;
    private double lastMouseX, lastMouseY;
    private WorldRaycaster.BlockRaycastResult hoveredBlock;

    private boolean isSwinging = false;
    private double swingTimer = 0.0;
    private boolean lastLeftClickDown;
    private boolean lastRightClickDown;
    private long lastRightClickRepeat;
    private final ItemSwapAnimationController itemSwapAnimationController = new ItemSwapAnimationController();
    private boolean repeatRightClick = false;
    private boolean justClicked;
    private boolean justRightClicked;
    private HotbarUI hotbarUi;
    private PlayerInventoryUI playerInventoryUI;
    private boolean inventoryIsOpen;
    private double mouseDx;
    private double mouseDy;

    public PlayerController(Player player, Window window) {
        this.player = player;
        this.window = window;
        this.hotbarUi = new HotbarUI(player, window);
        this.playerInventoryUI = new PlayerInventoryUI(player, window);
        window.setGrab(true);
        window.onKeyPress(GLFW_KEY_E, () -> {
            inventoryIsOpen = !inventoryIsOpen;
            window.setGrab(!inventoryIsOpen);
        });
        window.onKeyPress(GLFW_KEY_ESCAPE, () -> {
            inventoryIsOpen = false;
            window.setGrab(true);
        });
    }

    public Player getPlayer() {
        return player;
    }

    private ItemStack lastItem;

    public void update(double deltaTime, Window window) {
        updateInputState(window);
        if (window.isGrabbed()) {
            handleWalking(deltaTime);
            handleMouseTurning();
            hitOnLeftClick();
            placeBlocksOnRightClick();
            changeHotbarSlotOnNumberKeypresses(window);
            changeHotbarSlotWithMouseWheel(window);
            breakBlockOnRightClick(window);
        }
        itemSwapAnimationController.update(deltaTime);
        hoveredBlock = getPlayer().raycast(3.6);
        swingWhileBreaking();
        triggerSwapAnimationIfItemChanged();
        // Atualizar o swing
        updateSwing(deltaTime);
        if (!inventoryIsOpen) {
            var cursorItem = playerInventoryUI.consumeMovingItem();
            if (cursorItem != null) {
                player.getInventory().add(cursorItem);
            }
        }
    }

    private void hitOnLeftClick() {
        if (justClicked) {
            triggerSwing(0.15, 0.0);
            // TODO: Dar hit em outras entidades
        }
    }

    private void updateInputState(Window window) {
        justClicked = !lastLeftClickDown && window.isMouseDown(0);
        justRightClicked = !lastRightClickDown && window.isMouseDown(1);
        lastLeftClickDown = window.isMouseDown(0);
        lastRightClickDown = window.isMouseDown(1);
        var now = System.currentTimeMillis();
        if (justRightClicked) {
            lastRightClickRepeat = now;
        }
        if (!justRightClicked && window.isMouseDown(1) && now - lastRightClickRepeat > 1000 / 5) {
            lastRightClickRepeat = now;
            repeatRightClick = true;
        }
        double[] pos = window.getCursorPos();
        mouseDx = pos[0] - lastMouseX;
        mouseDy = pos[1] - lastMouseY;

        lastMouseX = pos[0];
        lastMouseY = pos[1];
    }

    private void placeBlocksOnRightClick() {
        var itemInHand = player.getItemInHand();
        var itemInHandAssociatedBlock = itemInHand == null ? null : itemInHand.type().getAssociatedBlock();

        if ((justRightClicked || repeatRightClick) && hoveredBlock != null && itemInHand != null && itemInHandAssociatedBlock != null) {
            var world = player.getWorld();
            var hitFace = hoveredBlock.getHitFace();
            var newBlockPosition = new Vector3i(hoveredBlock.blockPosition().x + hitFace.getOffsetX(), hoveredBlock.blockPosition().y + hitFace.getOffsetY(), hoveredBlock.blockPosition().z + hitFace.getOffsetZ());
            var blockAABB = new AABBd(newBlockPosition);
            var playerAABB = player.getAABB();
            if (!blockAABB.intersects(playerAABB)) {
                triggerSwing(0.15, 0.0);
                world.setBlock(newBlockPosition, itemInHandAssociatedBlock);
                player.decreaseItemInHand((short) 1);
            }
        }
    }

    private void changeHotbarSlotWithMouseWheel(Window window) {
        if (window.getScroll() != 0)
            player.getHotbar().moveSelectedWrapping((int) Math.floor(window.getScroll()));
    }

    private void changeHotbarSlotOnNumberKeypresses(Window window) {
        for (byte n_key = 0; n_key < player.getHotbar().getSlots(); n_key++) {
            if (window.isKeyPressed(GLFW_KEY_1 + n_key)) {
                player.getHotbar().setSelectedPosition(n_key);
            }
        }
    }

    private void triggerSwapAnimationIfItemChanged() {
        var itemInHand = player.getItemInHand();
        var lastItemType = lastItem == null ? null : lastItem.type();
        var newItemType = itemInHand == null ? null : itemInHand.type();
        if (!Objects.equals(lastItemType, newItemType)) {
            System.out.println("swap");
            itemSwapAnimationController.triggerSwap(itemInHand, lastItem);
        }
        lastItem = player.getItemInHand();
    }

    private void swingWhileBreaking() {
        if (hoveredBlock != null && player.isBreakingBlock(hoveredBlock.blockPosition())) {
            triggerSwing(0.20, 0.01);
        }
    }

    private void breakBlockOnRightClick(Window window) {
        if (window.isMouseDown(0)) {
            if (hoveredBlock != null && !player.isBreakingBlock(hoveredBlock.blockPosition())) {
                player.startBreakingBlock(hoveredBlock.blockPosition());
            } else if (hoveredBlock == null) {
                player.stopBreakingBlock();
            }
        } else {
            player.stopBreakingBlock();
        }
    }

    public void triggerSwing(double allowEarlyReswing, double resetTo) {
        if (isSwinging && swingTimer < allowEarlyReswing) return; // já está a decorrer
        swingTimer = resetTo;
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


        Vector2d rotation = player.getRotation();
        rotation.x += -mouseDy * SENSITIVITY;
        rotation.y += -mouseDx * SENSITIVITY;
        rotation.x = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, rotation.x));
    }

    public WorldRaycaster.BlockRaycastResult getHoveredBlock() {
        return hoveredBlock;
    }

    public record BreakingBlock(BlockType block, Vector3i position, double progress) {
    }

    public void renderFirstPersonView(ItemModelRegistry itemModelRegistry, Window window) {
        var itemInHand = itemSwapAnimationController.getItemToRender(player.getItemInHand());
        if (itemInHand != null) {
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
            transform.translate(0, itemSwapAnimationController.getCurrentOffsetY(), 0);
            itemModelRegistry.renderInFirstPersonView(
                    itemInHand.type(),
                    transform,
                    new Camera.Perspective(70),
                    window
            );
        }
        hotbarUi.render(itemModelRegistry);
        if (inventoryIsOpen) {
            playerInventoryUI.render(itemModelRegistry, justClicked);
        }
    }


}
