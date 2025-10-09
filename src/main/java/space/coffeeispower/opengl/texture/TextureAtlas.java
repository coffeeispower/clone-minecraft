package space.coffeeispower.opengl.texture;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class TextureAtlas {
    private final Texture texture;
    private final Map<String, UVCoords> uvMap = new HashMap<>();

    public TextureAtlas(String ...resourcePaths) throws IOException {
        Map<String, ImageData> images = new HashMap<>();

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

        // Cria ImageData final e Texture
        ImageData atlasData = new ImageData(atlasBuffer, totalWidth, maxHeight);
        this.texture = new Texture(atlasData);
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

    public Texture texture() {
        return texture;
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
}
