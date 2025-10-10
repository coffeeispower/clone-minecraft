package space.coffeeispower.opengl.texture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import space.coffeeispower.opengl.TextureBindGuard;

import java.io.Closeable;
import java.io.IOException;

public class Texture implements Closeable {
    private int id;

    public Texture(String resourcePath) throws IOException {
        this(ImageData.loadImageDataFromResource(resourcePath));
    }

    /**
     * Carrega uma imagem em RAM para a VRAM na GPU
     * @param data O conteudo da imagem
     * */
    public Texture(ImageData data) {
        createTexture(data);
    }


    private void createTexture(ImageData data) {
        id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                data.width(), data.height(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.data());

    }
    public TextureBindGuard bind(int slot) {
        return new TextureBindGuard(id, (byte) slot);
    }

    public int getId() {
        return id;
    }

    @Override
    public void close() {
        GL33.glDeleteTextures(id);
    }
}
