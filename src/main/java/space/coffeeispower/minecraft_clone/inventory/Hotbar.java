package space.coffeeispower.minecraft_clone.inventory;

import space.coffeeispower.minecraft_clone.item.ItemStack;

public class Hotbar {
    private final Inventory inventory;
    private final int start;
    private final int slots;
    private byte currentHotbarSlot;

    public Hotbar(Inventory inventory, int start, byte slots) {
        if (inventory.getSlotCount() < start + slots) {
            throw new IllegalArgumentException("A hotbar começa no slot " + start + " e acaba no slot " + (start + slots - 1) + " do inventário mas o inventário apenas tem " + inventory.getSlotCount() + " slots.");
        }
        this.start = start;
        this.inventory = inventory;
        this.slots = slots;
    }

    public byte getSelectedPosition() {
        return currentHotbarSlot;
    }

    public ItemStack setSelectedPosition(byte hotbarPosition) {
        if (hotbarPosition > slots) {
            throw new IllegalArgumentException("A hotbar apenas tem " + slots + "slots, mas tentou selecionar o slot " + hotbarPosition);
        }
        this.currentHotbarSlot = hotbarPosition;
        return inventory.getItem(getPositionInInventory());
    }

    public int getPositionInInventory() {
        return start + currentHotbarSlot;
    }

    public ItemStack getSelectedItem() {
        return inventory.getItem(getPositionInInventory());
    }

    public ItemStack setSelectedItem(ItemStack item) {
        inventory.setItem(getPositionInInventory(), item);
        return item;
    }
}
