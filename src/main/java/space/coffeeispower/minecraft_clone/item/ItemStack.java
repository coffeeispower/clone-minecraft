package space.coffeeispower.minecraft_clone.item;

public record ItemStack(ItemType type, short amount) {
    public ItemStack {
        if (amount < 0 || amount > type.getStackingLimit()) {
            throw new IllegalArgumentException("O item " + type.getDisplayName() + " só permite ter " + type.getStackingLimit() + " itens num único slot, mas tentou juntar " + amount + " num único slot.");
        }
    }

}
