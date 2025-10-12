package space.coffeeispower.minecraft_clone.inventory;

import space.coffeeispower.minecraft_clone.item.ItemStack;
import space.coffeeispower.minecraft_clone.item.ItemType;

public class Inventory {
    private final ItemStack[] items;

    public Inventory(int size) {
        items = new ItemStack[size];
    }

    public void setItem(int position, ItemStack item) {
        items[position] = item;
    }

    public ItemStack getItem(int position) {
        return items[position];
    }

    public ItemStack setItemAmount(int position, short amount) {
        return items[position] = new ItemStack(items[position].type(), amount);
    }

    public ItemStack increaseItem(int position, short amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("increaseItem chamado com amount negativo");
        }
        var oldItem = getItem(position);

        if (oldItem == null)
            throw new IllegalStateException("Tentou aumentar a quantidade num slot vazio (" + position + ")");

        return items[position] = new ItemStack(oldItem.type(), (short) (oldItem.amount() + amount));
    }

    public ItemStack decreaseItem(int position, short amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("decreaseItem chamado com amount negativo");
        }
        var oldItem = getItem(position);
        if (oldItem == null)
            throw new IllegalStateException("Tentou diminuir a quantidade num slot vazio (" + position + ")");
        short newAmount = (short) (oldItem.amount() - amount);
        if (newAmount == 0) {
            items[position] = null;
            return null;
        }
        return items[position] = new ItemStack(oldItem.type(), (short) (oldItem.amount() - amount));
    }

    public int getSlotCount() {
        return items.length;
    }


    /**
     * Tenta adicionar o ItemStack ao inventário.
     *
     * @param item ItemStack a adicionar.
     * @return O que não coube (novo ItemStack) ou null se coube tudo.
     */
    public ItemStack add(ItemStack item) {
        if (item == null)
            return null;

        ItemType type = item.type();
        short remaining = item.amount();
        short maxStack = type.getStackingLimit();

        // Tentar empilhar em stacks existentes do mesmo tipo
        for (int i = 0; i < items.length && remaining > 0; i++) {
            var stack = items[i];
            if (stack == null || stack.type() != type)
                continue;

            short space = (short) (maxStack - stack.amount());
            if (space > 0) {
                short toAdd = (short) Math.min(space, remaining);
                items[i] = new ItemStack(type, (short) (stack.amount() + toAdd));
                remaining -= toAdd;
            }
        }

        // Colocar o resto em slots vazios
        for (int i = 0; i < items.length && remaining > 0; i++) {
            if (items[i] != null)
                continue;

            short toAdd = (short) Math.min(maxStack, remaining);
            items[i] = new ItemStack(type, toAdd);
            remaining -= toAdd;
        }

        // Se sobrar, devolver o que não coube
        if (remaining > 0)
            return new ItemStack(type, remaining);

        return null;
    }
}
