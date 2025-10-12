package space.coffeeispower.minecraft_clone.opengl.texture.t2d;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import space.coffeeispower.minecraft_clone.opengl.texture.ImageData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static space.coffeeispower.minecraft_clone.opengl.texture.t2d.ImageTexture.create2DTexture;

public final class TextureAtlas implements Texture2D {

    private final int id;
    private final int width;
    private final int height;
    private final Map<String, UVCoords> uvMap = new HashMap<>();

    public TextureAtlas(String ...resourcePaths) throws IOException {
        var data = generateAtlasImage(resourcePaths, uvMap);
        this.width = data.width();
        this.height = data.height();
        this.id = create2DTexture(data);
    }

    @NotNull
    private static ImageData generateAtlasImage(String[] resourcePaths, Map<String, UVCoords> uvMap) throws IOException {
        Map<String, ImageData> images = new LinkedHashMap<>();

        int totalWidth = 0;
        int maxHeight = 0;
        for (String path : resourcePaths) {
            ImageData img = ImageData.loadImageDataFromResource(path);
            images.put(path, img);
            totalWidth += img.width();
            maxHeight = Math.max(maxHeight, img.height());
        }

        // Cria buffer para o atlas completo (RGBA → 4 bytes por pixel)
        ByteBuffer atlasBuffer = BufferUtils.createByteBuffer(totalWidth * maxHeight * 4);

        int currentX = 0;
        for (var entry : images.entrySet()) {
            String path = entry.getKey();
            ImageData img = entry.getValue();


            copyImageDataToBuffer(img, atlasBuffer, currentX, totalWidth);

            // Calcula UV normalizados
            float u0 = (float) currentX / totalWidth;
            float v0 = 0f;
            float u1 = (float) (currentX + img.width()) / totalWidth;
            float v1 = (float) img.height() / maxHeight;

            uvMap.put(path, new UVCoords(u0, v0, u1, v1));

            currentX += img.width();
        }

        atlasBuffer.flip();

        return new ImageData(atlasBuffer, totalWidth, maxHeight, false);
    }

    private static void copyImageDataToBuffer(ImageData srcImg, ByteBuffer target, int offsetX, int targetWidth) {
        // Copiar linha a linha
        for (int y = 0; y < srcImg.height(); y++) {
            int srcPos = (y * srcImg.width()) * 4;
            int dstPos = ((y * targetWidth) + offsetX) * 4;

            ByteBuffer src = srcImg.data().duplicate();
            src.position(srcPos).limit(srcPos + srcImg.width() * 4);
            target.position(dstPos);
            target.put(src);
        }
    }


    public UVCoords getUV(String path) {
        return uvMap.get(path);
    }

    public float[] topLeft(String path) {
        var uv = uvMap.get(path);
        return new float[]{uv.left(), uv.top()};
    }

    public float[] topRight(String path) {
        var uv = uvMap.get(path);
        return new float[]{uv.right(), uv.top()};
    }

    public float[] bottomLeft(String path) {
        var uv = uvMap.get(path);
        return new float[]{uv.left(), uv.bottom()};
    }

    public float[] bottomRight(String path) {
        var uv = uvMap.get(path);
        return new float[]{uv.right(), uv.bottom()};
    }

    public record UVCoords(float left, float bottom, float right, float top) {}

    @Override
    public int getTextureId() {
        return id;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }
}
