package space.coffeeispower.minecraft_clone.item;

import org.jspecify.annotations.Nullable;
import space.coffeeispower.minecraft_clone.world.block.BlockType;

public enum ItemType {
    Bedrock("Bedrock", BlockType.Bedrock, (short) 64),
    Dirt("Terra", BlockType.Dirt, (short) 64),
    Grass("Grama", BlockType.Grass, (short) 64),
    Stone("Pedra", BlockType.Stone, (short) 64),
    DiamondSword("Espada de Diamante", null, (short) 1);

    private final String displayName;
    private final BlockType associatedBlock; // Se for um bloco colocável
    private final short stackingLimit;
    /**
     * @param displayName   Nome do item
     * @param stackingLimit O maximo que o item pode juntar num único slot do inventário
     */
    ItemType(String displayName, @Nullable BlockType associatedBlock, short stackingLimit) {
        this.displayName = displayName;
        this.associatedBlock = associatedBlock;
        this.stackingLimit = stackingLimit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BlockType getAssociatedBlock() {
        return associatedBlock;
    }

    public short getStackingLimit() {
        return stackingLimit;
    }
}
