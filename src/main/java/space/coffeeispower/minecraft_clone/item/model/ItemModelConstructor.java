package space.coffeeispower.minecraft_clone.item.model;

import java.io.IOException;

@FunctionalInterface
public interface ItemModelConstructor<T extends ItemModel> {
    T get() throws IOException;
}
