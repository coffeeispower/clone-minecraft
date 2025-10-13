package space.coffeeispower.minecraft_clone.ui;

import org.joml.Vector4i;
import space.coffeeispower.minecraft_clone.entity.player.Player;
import space.coffeeispower.minecraft_clone.item.ItemStack;
import space.coffeeispower.minecraft_clone.item.view.ItemModelRegistry;
import space.coffeeispower.minecraft_clone.opengl.Draw;
import space.coffeeispower.minecraft_clone.window.Window;

public class PlayerInventoryUI {

    private final Player player;
    private final Window window;
    private ItemStack movingItemStack;
    public PlayerInventoryUI(Player player, Window window) {
        this.player = player;
        this.window = window;
    }

    public ItemStack consumeMovingItem() {
        var item = movingItemStack;
        movingItemStack = null;
        return item;
    }

    public void render(ItemModelRegistry itemModelRegistry, boolean justLeftClicked) {
        var inventory = player.getInventory();
        if (inventory == null) return;

        // === Configuração base ===
        var cols = 9;
        var rows = Math.max(1, inventory.getSlotCount() / cols);
        var slotSize = 55;
        var gap = 8;

        var totalWidth = cols * slotSize + (cols - 1) * gap;
        var totalHeight = rows * slotSize + (rows - 1) * gap;

        // Centro da janela é (0,0), portanto:
        var startX = -totalWidth / 2.0;
        var startY = totalHeight / 2.0; // topo da grelha
        Draw.rectangle(new Vector4i(0, 0, 0, 160), -window.width() / 2., window.height() / 2., window.width(), window.height());
        var bgPadding = 16;
        var bgWidth = totalWidth + bgPadding * 2;
        var bgHeight = totalHeight + bgPadding * 2;
        Draw.rectangle(new Vector4i(120, 120, 120, 255), -bgWidth / 2., bgHeight / 2., bgWidth, bgHeight);
        var cursorPos = window.getCursorPos();
        int slotIteration = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (slotIteration >= inventory.getSlotCount()) break;

                double x = startX + col * (slotSize + gap);
                double y = startY - row * (slotSize + gap); // y positivo é para cima

                // Desenhar fundo da slot
                Draw.rectangle(new Vector4i(0, 0, 0, 80), x, y, slotSize, slotSize);
                Draw.rectangleBorder(new Vector4i(100, 100, 100, 255), x, y, slotSize, slotSize, 2);

                var actualSlot = slotIteration;

                // Faz a hotbar aparecer na ultima linha
                if (slotIteration < 9) {
                    actualSlot += 9 * 3;
                } else if (slotIteration >= 9 * 3) {
                    actualSlot -= 9 * 3;
                }
                var item = inventory.getItem(actualSlot);
                if (item != null) {
                    // Renderiza o item no centro do quadrado
                    itemModelRegistry.renderInInventory(
                            item,
                            x + slotSize / 2.0,
                            y - slotSize / 2.0
                    );
                }
                var hover =
                        cursorPos[0] > x + window.width() / 2 && cursorPos[0] < x + window.width() / 2 + slotSize &&
                                window.height() - cursorPos[1] > y + window.height() / 2 - slotSize && window.height() - cursorPos[1] < y + window.height() / 2;
                if (hover) {
                    Draw.rectangle(new Vector4i(255, 255, 255, 80), x, y, slotSize, slotSize);
                    if (justLeftClicked) {
                        var tmp = inventory.getItem(actualSlot);
                        inventory.setItem(actualSlot, movingItemStack);
                        movingItemStack = tmp;
                    }
                }

                slotIteration++;
            }
        }
        if (movingItemStack != null) {
            // Renderiza o item no centro do quadrado
            itemModelRegistry.renderInInventory(
                    movingItemStack,
                    cursorPos[0] - window.width() / 2,
                    window.height() / 2 - cursorPos[1]

            );
        }
    }
}

