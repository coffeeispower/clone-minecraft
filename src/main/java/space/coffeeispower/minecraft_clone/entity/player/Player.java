package space.coffeeispower.minecraft_clone.entity.player;

import org.joml.Vector2d;
import org.joml.Vector3d;
import space.coffeeispower.minecraft_clone.entity.*;
import space.coffeeispower.minecraft_clone.inventory.Hotbar;
import space.coffeeispower.minecraft_clone.inventory.Inventory;
import space.coffeeispower.minecraft_clone.item.ItemStack;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.raycast.WorldRaycaster;
import space.coffeeispower.minecraft_clone.world.World;

public class Player extends FallingCollidingEntity<Player> implements HasInventory, HasHotBar, HasHand {
    private final Camera eye;
    private final Inventory inventory = new Inventory(9 * 4);
    private final Hotbar hotbar = new Hotbar(inventory, 0, (byte) 9);
    public Player(Vector3d position, World world) {
        super(position, new Vector3d(0.7, 1.8, 0.7), world);
        eye = new Camera(getEyePosition(), new Camera.Perspective(70));
    }

    public Vector3d getEyePosition() {
        return position.add(0, 0.75, 0, new Vector3d());
    }
    @Override
    public EntityRenderer<Player> getRenderer() {
        return null;
    }
    public double ticksOnGround;
    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        if(isOnGround()) {
            ticksOnGround += deltaTime / (1./20.);
        } else {
            ticksOnGround = 0;
        }
        eye.position().set(position.x, position.y + 0.75, position.z);
        eye.rotation().set(getRotation().x, getRotation().y, 0);
    }
    public void walk(Vector2d direction, double deltaTime) {
        if (direction.lengthSquared() == 0) return;

        Vector2d forward = getForward();
        Vector2d right = getRight();

        // direção desejada
        Vector2d inputDir = new Vector2d();
        forward.mul(-direction.y, inputDir).add(right.mul(direction.x, new Vector2d()));

        if (inputDir.lengthSquared() == 0) return;

        inputDir.normalize();

        Vector3d motion = getMotion();
        Vector2d horizontal = new Vector2d(motion.x, motion.z);

        // aceleração dependente se está no chão ou no ar
        double accel = isOnGround() ? 90 : 2*12; // aqui estamos a aumentar a aceleração para escala de blocos/segundo²
        inputDir.mul(accel * deltaTime);

        horizontal.add(inputDir);

        // limitar velocidade horizontal
        double maxSpeed = isOnGround() ? 5*4 : 3*6; // blocos por segundo
        if (horizontal.length() > maxSpeed) {
            horizontal.normalize().mul(maxSpeed);
        }

        motion.x = horizontal.x;
        motion.z = horizontal.y;
    }




    public boolean canJump() {
        return ticksOnGround > 0.5;
    }
    private Vector2d getForward() {
        double cosPitch = Math.cos(-rotation.x);
        return new Vector2d(
                Math.sin(-rotation.y) * cosPitch,
                -Math.cos(-rotation.y) * cosPitch
        ).normalize();
    }

    private Vector2d getRight() {
        return new Vector2d(
                Math.cos(-rotation.y),
                Math.sin(-rotation.y)
        ).normalize();
    }

    public Camera getEye() {
        return eye;
    }

    public WorldRaycaster.BlockRaycastResult raycast(double maxDistance) {
        return WorldRaycaster.findClosestBlock(this.getEyePosition(), this.getLookDirection(), this.getWorld(), maxDistance);
    }

    @Override
    public Hotbar getHotbar() {
        return hotbar;
    }

    @Override
    public ItemStack increaseItemInHand(short amount) {
        return inventory.increaseItem(hotbar.getPositionInInventory(), amount);
    }

    @Override
    public ItemStack decreaseItemInHand(short amount) {
        return inventory.decreaseItem(hotbar.getPositionInInventory(), amount);
    }

    @Override
    public ItemStack setItemAmountInHand(short amount) {
        return inventory.setItemAmount(hotbar.getPositionInInventory(), amount);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
