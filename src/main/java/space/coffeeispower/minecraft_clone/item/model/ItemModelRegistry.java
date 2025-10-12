package space.coffeeispower.minecraft_clone.item.model;

import org.joml.Matrix4d;
import space.coffeeispower.minecraft_clone.item.ItemType;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.TextureAtlas;
import space.coffeeispower.minecraft_clone.window.Window;

import java.io.IOException;
import java.util.HashMap;

public class ItemModelRegistry {
    public HashMap<ItemType, ItemModel> itemModels = new HashMap<>();

    private <T extends ItemModel> void loadItemModel(ItemType itemType, ItemModelConstructor<T> model) {
        try {
            itemModels.put(itemType, model.get());
        } catch (IOException e) {
            throw new ItemModelLoadError(itemType, e);
        }
    }

    private void loadBlockItemModel(ItemType type) {
        var block = type.getAssociatedBlock();
        if (block == null) {
            throw new IllegalArgumentException("O item " + type.name() + " não é um bloco");
        }
        loadItemModel(type, () -> new BlockItemModel(block));
    }

    private void load2DItemModel(ItemType type, String texturePath) {
        loadItemModel(type, () -> new Item2DModel(texturePath));
    }

    public ItemModelRegistry() {
        loadBlockItemModel(ItemType.Dirt);
        loadBlockItemModel(ItemType.Bedrock);
        loadBlockItemModel(ItemType.Stone);
        loadBlockItemModel(ItemType.Grass);
        load2DItemModel(ItemType.DiamondSword, "/textures/items/diamond_sword.png");
    }

    public void renderInFirstPersonView(ItemType item, Matrix4d transform, Camera.Perspective perspective, Window window) {
        itemModels.get(item).renderInFirstPersonView(transform, perspective, window);
    }

    public void renderInInventory(ItemType item, Matrix4d transform) {
        itemModels.get(item).renderAsUi(transform);
    }
}
