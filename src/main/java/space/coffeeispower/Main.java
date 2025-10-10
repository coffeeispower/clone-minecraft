package space.coffeeispower;

import space.coffeeispower.window.Window;

public final class Main {
    public static void main(String[] args) {
        try (var game = new Game(Window::new)) {
            game.loop();
        } catch (Exception e) {
            System.err.println("O jogo crashou: " + e);
            e.printStackTrace();
        }
    }
}
