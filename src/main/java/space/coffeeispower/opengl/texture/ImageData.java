package space.coffeeispower.opengl.texture;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public record ImageData(ByteBuffer data, int width, int height) {
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
            data = new ImageData(imageData, width.get(0), height.get(0));
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
}
