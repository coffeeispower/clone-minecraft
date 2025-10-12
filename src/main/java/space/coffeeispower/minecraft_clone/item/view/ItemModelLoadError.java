package space.coffeeispower.minecraft_clone.item.view;

import space.coffeeispower.minecraft_clone.item.ItemType;

import java.io.IOException;

public class ItemModelLoadError extends RuntimeException {
    public ItemModelLoadError(ItemType item, IOException e) {
        super("Falha ao carregar o modelo do item " + item.name(), e);
    }
}
