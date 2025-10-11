package space.coffeeispower.minecraft_clone.world.block.breaking;

import space.coffeeispower.minecraft_clone.entity.Entity;

public final class BlockBreakState {
    private final Entity<?> breaker;
    private final double breakSpeed;
    private double progress;

    public BlockBreakState(Entity<?> breaker, double breakSpeed) {
        this.breaker = breaker;
        this.breakSpeed = breakSpeed;
    }


    public Entity<?> getBreaker() {
        return breaker;
    }

    public double getBreakSpeed() {
        return breakSpeed;
    }

    public double getProgress() {
        return progress;
    }


    public boolean update(double deltaTime) {
        progress += breakSpeed * deltaTime;
        return progress >= 1.0; // retorna true se terminou
    }
}
