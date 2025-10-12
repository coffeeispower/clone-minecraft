package space.coffeeispower.minecraft_clone.opengl.texture;

import org.joml.Vector4i;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public record ImageData(ByteBuffer data, int width, int height, boolean isStb) implements AutoCloseable {
    public static ImageData loadImageDataFromResource(String resourcePath) throws IOException {
        ImageData data;
        // Carrega recurso como InputStream
        try (var in = ImageData.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new IOException("Recurso não encontrado: " + resourcePath);

            ByteBuffer imageBuffer = ioResourceToByteBuffer(in);

            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);

            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer imageData = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);
            if (imageData == null) throw new RuntimeException("Falha ao carregar textura: " + STBImage.stbi_failure_reason());
            data = new ImageData(imageData, width.get(0), height.get(0), true);
        }
        return data;
    }

    // Converte InputStream em ByteBuffer para STB
    private static ByteBuffer ioResourceToByteBuffer(InputStream source) throws IOException {
        try (ReadableByteChannel rbc = Channels.newChannel(source)) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(16 * 1024); // buffer inicial
            while (true) {
                int bytes = rbc.read(buffer);
                if (bytes == -1) break;
                if (buffer.remaining() == 0) {
                    ByteBuffer newBuffer = BufferUtils.createByteBuffer(buffer.capacity() * 2);
                    buffer.flip();
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                }
            }
            buffer.flip();
            return buffer;
        }
    }

    public Vector4i getPixelColor(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return new Vector4i();

        // Cada pixel = 4 bytes (R, G, B, A)
        int index = (y * width + x) * 4;

        // ler bytes e converter para 0..1
        int r = (data.get(index) & 0xFF);
        int g = (data.get(index + 1) & 0xFF);
        int b = (data.get(index + 2) & 0xFF);
        int a = (data.get(index + 3) & 0xFF);

        return new Vector4i(r, g, b, a);
    }

    @Override
    public void close() {
        if (isStb)
            STBImage.stbi_image_free(data);
    }
}
