package space.coffeeispower.minecraft_clone.entity;

import space.coffeeispower.minecraft_clone.item.ItemStack;

public interface HasHand {
    ItemStack getItemInHand();

    ItemStack setItemInHand(ItemStack item);

    ItemStack increaseItemInHand(short amount);

    ItemStack decreaseItemInHand(short amount);

    ItemStack setItemAmountInHand(short amount);
}
