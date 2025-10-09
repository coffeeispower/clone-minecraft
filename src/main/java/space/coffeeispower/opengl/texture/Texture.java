package space.coffeeispower.opengl.texture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import space.coffeeispower.opengl.TextureBindGuard;

import java.io.IOException;

public class Texture {
    private int id;

    public Texture(String resourcePath) throws IOException {
        createTexture(ImageData.loadImageDataFromResource(resourcePath));
    }

    /**
     * Carrega uma imagem em RAM para a VRAM na GPU
     * @param data O conteudo da image, este construtor pega ownership deste data e dá free no fim, por tanto não uses este data depois de chamar este construtor
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

        STBImage.stbi_image_free(data.data());
    }
    public TextureBindGuard bind(int slot) {
        return new TextureBindGuard(id, (byte) slot);
    }

    public int getId() {
        return id;
    }

}
