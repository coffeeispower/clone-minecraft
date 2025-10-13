package space.coffeeispower.minecraft_clone.entity.player.animation;

import space.coffeeispower.minecraft_clone.item.ItemStack;

public class ItemSwapAnimationController {

    private static final double SWAP_DURATION = 0.15;

    private ItemStack oldItem;   // item atualmente a desaparecer
    private ItemStack newItem;   // item que vai aparecer
    private boolean isActive = false;
    private boolean descending = true;
    private double timer = 0.0;
    private double currentOffsetY = 0.0;

    public void triggerSwap(ItemStack nextItem, ItemStack currentItem) {
        if (!isActive) {
            oldItem = currentItem;
            newItem = nextItem;
            timer = 0.0;

            if (oldItem != null) {
                // Descer o item antigo
                descending = true;
                currentOffsetY = 0.0;
            } else {
                // Não há item antigo, sobe o item novo diretamente
                descending = false;
                currentOffsetY = -1.5;
            }

            isActive = true;
        } else {
            // Já está a animar
            if (!descending) {
                // Se estava a subir, iniciar descida a partir da posição atual
                descending = true;
                timer = 0.0;
                oldItem = currentItem;
                // currentOffsetY mantido para descer suavemente
            }
            newItem = nextItem;
        }
    }

    public void update(double deltaTime) {
        if (!isActive) return;

        timer += deltaTime;

        double progress = Math.min(timer / SWAP_DURATION, 1.0);
        if (descending) {
            currentOffsetY = -1.5 * progress;

            if (progress >= 1.0) {
                timer = 0.0;
                descending = false;

                if (newItem == null) {
                    // Apenas descer o item antigo e parar
                    isActive = false;
                    currentOffsetY = -1.5;
                    oldItem = null;
                } else {
                    // Começar a subir o novo item
                    oldItem = null;
                }
            }
        } else {
            currentOffsetY = -1.5 * (1.0 - progress);

            if (progress >= 1.0) {
                isActive = false;
                timer = 0.0;
                currentOffsetY = 0.0;
                oldItem = null;
                newItem = null;
            }
        }
    }

    public double getCurrentOffsetY() {
        return currentOffsetY;
    }

    public ItemStack getItemToRender(ItemStack currentHandItem) {
        if (!isActive) return currentHandItem;

        if (descending) {
            return oldItem; // item antigo durante descida
        } else {
            return newItem != null ? newItem : currentHandItem; // item novo durante subida ou item atual se newItem for null
        }
    }

    public boolean isActive() {
        return isActive;
    }
}
