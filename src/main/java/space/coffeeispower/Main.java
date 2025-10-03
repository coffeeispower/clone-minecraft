package space.coffeeispower;

import space.coffeeispower.window.Window;

public class Main {
    public static void main(String[] args) {
        try (var game = new Game(Window::new)) {
            game.loop();
        }
    }
}
