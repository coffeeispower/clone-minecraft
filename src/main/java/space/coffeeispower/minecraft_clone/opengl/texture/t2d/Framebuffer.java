package space.coffeeispower.minecraft_clone.opengl.texture.t2d;

import org.joml.Vector2i;
import org.lwjgl.system.MemoryStack;
import space.coffeeispower.minecraft_clone.resources.InfallibleAutoClose;
import space.coffeeispower.minecraft_clone.window.Window;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.*;

public class Framebuffer implements Texture2D {
    private final int depthRenderBufferId;
    private final int fboId;
    private final int textureId;
    private final int width;
    private final int height;
    private boolean fbDropped;
    private boolean closed;

    public Framebuffer() {
        this(getViewportSize());
    }

    private static Vector2i getViewportSize() {
        var size = new Vector2i();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer prev = stack.callocInt(4);
            glGetIntegerv(GL_VIEWPORT, prev);
            size.x = prev.get(2);
            size.y = prev.get(3);
        }
        return size;
    }

    public Framebuffer(Vector2i size) {
        this(size.x, size.y);
    }

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
        int previousRbo = glGetInteger(GL_RENDERBUFFER_BINDING);
        var previousTextureId = glGetInteger(GL_TEXTURE_BINDING_2D);
        try {
            // Criar FBO
            fboId = glGenFramebuffers();
            glBindFramebuffer(GL_FRAMEBUFFER, fboId);

            // Criar textura para color attachment
            textureId = createEmptyTexture(width, height);

            // Anexar textura ao framebuffer
            attachTextureToFramebuffer();

            // Criar renderbuffer para depth/stencil
            depthRenderBufferId = createDepthBuffer(width, height);

            // Verificar se framebuffer está completo
            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Framebuffer incompleto!");
            }
        } catch (RuntimeException e) {
            close();
            throw e;
        } finally {
            glBindRenderbuffer(GL_RENDERBUFFER, previousRbo);
            glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
            glBindTexture(GL_TEXTURE_2D, previousTextureId);
        }

    }

    private int createDepthBuffer(int width, int height) {
        final int rboId;
        rboId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rboId);
        return rboId;
    }

    private void attachTextureToFramebuffer() {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
    }

    private int createEmptyTexture(int width, int height) {
        final int textureId;
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        return textureId;
    }

    /**
     * Faz bind deste framebuffer para renderização
     */
    public FramebufferBindGuard bindForDrawing() {
        if (fbDropped) {
            throw new RuntimeException("O framebuffer " + fboId + " já foi dropado");
        }
        return new FramebufferBindGuard(fboId, width, height);
    }

    @Override
    public InfallibleAutoClose bindTextureOnSlot(int slot) {
        if (closed) {
            throw new RuntimeException("A textura " + this.textureId + " e o framebuffer " + fboId
                    + " já foram dropados.");
        }
        return Texture2D.super.bindTextureOnSlot(slot);
    }

    @Override
    public int getTextureId() {
        return textureId;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void close() {
        if (fbDropped || closed) return;
        glDeleteFramebuffers(fboId);
        glDeleteRenderbuffers(depthRenderBufferId);
        Texture2D.super.close();
        fbDropped = true;
        closed = true;
    }


    public Texture2D dropFramebuffer() {
        glDeleteFramebuffers(fboId);
        glDeleteRenderbuffers(depthRenderBufferId);
        fbDropped = true;
        return new ImageTexture(getTextureId(), width, height);
    }

    public Framebuffer ensureSize(int width, int height) {
        if (this.width != width || this.height != height) {
            this.close();
            return new Framebuffer(width, height);
        }
        return this;
    }

    public Framebuffer adjustSizeToWindow(Window window) {
        return ensureSize(window.width(), window.height());
    }

    public static Framebuffer adjustSizeToWindow(Framebuffer fb, Window window) {
        if (fb == null) {
            return new Framebuffer(window.width(), window.height());
        }
        return fb.ensureSize(window.width(), window.height());
    }

    /**
     * Guard que restaura o framebuffer antigo e a viewport
     */
    public static class FramebufferBindGuard implements AutoCloseable {
        private final int previousFbo;
        private final int previousViewportX;
        private final int previousViewportY;
        private final int previousViewportWidth;
        private final int previousViewportHeight;

        public FramebufferBindGuard(int fbo, int width, int height) {
            // Guardar estado atual
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer prev = stack.callocInt(4);
                glGetIntegerv(GL_VIEWPORT, prev);
                prev.rewind();
                previousViewportX = prev.get();
                previousViewportY = prev.get();
                previousViewportWidth = prev.get();
                previousViewportHeight = prev.get();
            }

            previousFbo = glGetInteger(GL_FRAMEBUFFER_BINDING);

            // Bind novo framebuffer
            glBindFramebuffer(GL_FRAMEBUFFER, fbo);
            glViewport(0, 0, width, height);

            glClearColor(0, 0, 0, 0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        @Override
        public void close() {
            glBindFramebuffer(GL_FRAMEBUFFER, previousFbo);
            glViewport(previousViewportX, previousViewportY, previousViewportWidth, previousViewportHeight);
        }
    }
}
