package space.coffeeispower.minecraft_clone.opengl.texture.t2d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import space.coffeeispower.minecraft_clone.opengl.texture.ImageData;

import java.io.IOException;

public class ImageTexture implements Texture2D {
    private final int id;
    private final int width;
    private final int height;

    public ImageTexture(String resourcePath) throws IOException {
        var data = ImageData.loadImageDataFromResource(resourcePath);
        width = data.width();
        height = data.height();
        id = create2DTexture(data);
        data.close();
    }

    /**
     * Carrega uma imagem em RAM para a VRAM na GPU
     *
     * @param data O conteudo da imagem
     */
    public ImageTexture(ImageData data) {
        width = data.width();
        height = data.height();
        id = create2DTexture(data);
    }

    ImageTexture(int id, int width, int height) {
        if (!GL30.glIsTexture(id)) {
            throw new RuntimeException("Textura " + id + " não existe");
        }
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public static int create2DTexture(ImageData data) {
        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                data.width(), data.height(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.data());
        return id;
    }

    public int getTextureId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
