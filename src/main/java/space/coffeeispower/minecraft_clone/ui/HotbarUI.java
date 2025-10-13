package space.coffeeispower.minecraft_clone.ui;

import org.joml.Vector4i;
import space.coffeeispower.minecraft_clone.entity.HasHotBar;
import space.coffeeispower.minecraft_clone.item.view.ItemModelRegistry;
import space.coffeeispower.minecraft_clone.opengl.Draw;
import space.coffeeispower.minecraft_clone.window.Window;

public class HotbarUI {
    private final HasHotBar entityWithHotbar;
    private final Window window;

    public HotbarUI(HasHotBar entityWithHotbar, Window window) {
        this.entityWithHotbar = entityWithHotbar;
        this.window = window;
    }

    public void render(ItemModelRegistry itemModelRegistry) {
        var gap = 8;
        var hotbar = entityWithHotbar.getHotbar();
        var slotCount = hotbar.getSlots();
        var slotSize = 55;
        var hotbarSize = (Math.max(slotCount - 1, 0) * gap) + slotSize * slotCount;
        var positionXCursor = 0;
        var hotbarPositionX = -hotbarSize / 2;
        var hotbarPositionY = -(window.height() / 2) + gap + slotSize;
        var i = 0;

        for (; i < slotCount; i++) {
            Draw.rectangle(new Vector4i(0, 0, 0, 60), hotbarPositionX + positionXCursor, hotbarPositionY, slotSize, slotSize);
            if (hotbar.getSelectedPosition() == i) {
                Draw.rectangleBorder(new Vector4i(255, 255, 255, 255), hotbarPositionX + positionXCursor, hotbarPositionY, slotSize, slotSize, 4);
            }
            positionXCursor += slotSize;
            var item = hotbar.getItemAtSlot(i);
            if (item != null) {
                itemModelRegistry.renderInInventory(item, hotbarPositionX + positionXCursor - (slotSize / 2.), hotbarPositionY - (slotSize / 2.));
            }

            var hasNext = i + 1 < slotCount;
            if (hasNext) {
                positionXCursor += gap;
            }
        }
    }
}
