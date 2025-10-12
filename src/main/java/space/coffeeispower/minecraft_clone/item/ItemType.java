package space.coffeeispower.minecraft_clone.item;

import org.jspecify.annotations.Nullable;
import space.coffeeispower.minecraft_clone.resources.InfallibleAutoClose;
import space.coffeeispower.minecraft_clone.world.block.BlockType;

public enum ItemType implements InfallibleAutoClose {
    Bedrock("Bedrock", BlockType.Bedrock),
    Dirt("Terra", BlockType.Dirt),
    Grass("Grama", BlockType.Grass),
    Stone("Pedra", BlockType.Stone),
    DiamondSword("Espada de Diamante", null);

    private final String displayName;
    private final BlockType associatedBlock; // Se for um bloco colocável

    /**
     * @param displayName Nome do item
     */
    ItemType(String displayName, @Nullable BlockType associatedBlock) {
        this.displayName = displayName;
        this.associatedBlock = associatedBlock;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BlockType getAssociatedBlock() {
        return associatedBlock;
    }

    @Override
    public void close() {

    }

}
