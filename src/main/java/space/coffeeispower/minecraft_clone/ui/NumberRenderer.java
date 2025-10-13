package space.coffeeispower.minecraft_clone.ui;

import org.joml.Matrix4d;
import space.coffeeispower.minecraft_clone.opengl.Draw;
import space.coffeeispower.minecraft_clone.resources.Resources;

public class NumberRenderer {
    public static void renderNumber(int number, double x, double y) {
        var digitWidth = 8;
        var digitHeight = digitWidth * 2;
        var numberToString = String.valueOf(number);
        var gap = 4;
        var textSize = (numberToString.length() - 1) * gap + numberToString.length() * digitWidth;
        var centeredX = x - textSize / 2.;
        var positionXCursor = centeredX;
        for (char digit : numberToString.toCharArray()) {
            var digitInt = digit - '0';
            Draw.texture(Resources.INSTANCE.numbersTextures[digitInt], new Matrix4d().translate(positionXCursor, y, 0).scaleXY(digitWidth, digitHeight));
            positionXCursor += digitWidth + gap;
        }
    }
}
