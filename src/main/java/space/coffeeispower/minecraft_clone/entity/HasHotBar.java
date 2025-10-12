package space.coffeeispower.minecraft_clone.entity;

import space.coffeeispower.minecraft_clone.inventory.Hotbar;
import space.coffeeispower.minecraft_clone.item.ItemStack;

public interface HasHotBar extends HasHand, HasInventory {
    Hotbar getHotbar();

    @Override
    default ItemStack getItemInHand() {
        return getHotbar().getSelectedItem();
    }

    @Override
    default ItemStack setItemInHand(ItemStack item) {
        return getHotbar().setSelectedItem(item);
    }
}
