package space.coffeeispower.minecraft_clone.debug;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.stb.STBImageWrite;
import space.coffeeispower.minecraft_clone.entity.player.Player;
import space.coffeeispower.minecraft_clone.opengl.Camera;
import space.coffeeispower.minecraft_clone.opengl.texture.t2d.Texture2D;
import space.coffeeispower.minecraft_clone.resources.Resources;
import space.coffeeispower.minecraft_clone.world.World;
import space.coffeeispower.minecraft_clone.world.chunk.Chunk;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class ExportWorldAsOBJ {

    public static void exportWorldAsObj(World world, Player player) {
        try {
            exportChunksOBJWithTexture(world.getLoadedChunks().toList(), "presentation/public/world.obj", "playerCamera.json", player.getEye(), "presentation/public/worldTextureAtlas.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Exporta todos os chunks para OBJ + textura + JSON da câmera
     */
    public static void exportChunksOBJWithTexture(
            List<World.LoadedChunk> loadedChunks,
            String objPath,
            String cameraJsonPath,
            Camera camera,
            String texturePath
    ) throws IOException {
        Texture2D texture = Resources.INSTANCE.blockTextureAtlas;
        Vector3d camPos = camera.position();
        Vector2f camRot = camera.rotation().xy(new Vector2f());
        FileWriter writer = new FileWriter(objPath);
        writer.write("# Exported map with texture\n");

        int vertexOffset = 1;

        for (var c: loadedChunks) {
            var chunkMesh = c.chunkMesh();

            var vertices = chunkMesh.vertices();
            for (int i = 0; i < vertices.length; i+=3) {
                double x = vertices[i];
                double y = vertices[i+1];
                double z = vertices[i+2];
                Vector2i chunkPosition = c.chunk().getPosition().mul(Chunk.CHUNK_WIDTH);
                writer.write(String.format("v %f %f %f\n", chunkPosition.x + x, y, chunkPosition.y + z));
            }
            var uvs = chunkMesh.uv();
            for (int i = 0; i < uvs.length; i+=2) {
                var u = uvs[i];
                var v = uvs[i+1];
                writer.write(String.format("vt %f %f\n", u, 1.0f - v)); // OBJ inverte V
            }

            // Faces sequenciais
            int numTriangles = vertices.length / 3 / 3;
            for (int i = 0; i < numTriangles; i++) {
                int i1 = vertexOffset + i * 3;
                int i2 = vertexOffset + i * 3 + 1;
                int i3 = vertexOffset + i * 3 + 2;
                writer.write(String.format("f %d/%d %d/%d %d/%d\n", i1, i1, i2, i2, i3, i3));
            }

            vertexOffset += vertices.length / 3;
        }
        writer.flush();
        writer.close();

        // Exporta câmera
        FileWriter camWriter = new FileWriter(cameraJsonPath);
        String json = String.format(
                "{\n  \"position\": [%f, %f, %f],\n  \"rotation\": [%f, %f]\n}",
                camPos.x, camPos.y, camPos.z,
                camRot.x, camRot.y
        );
        camWriter.write(json);
        camWriter.flush();
        camWriter.close();

        // Exporta textura
        exportTexture(texture, texturePath);

        System.out.println("Export completed: OBJ, Camera JSON and Texture saved");
    }

    /**
     * Lê uma Texture2D da GPU e salva com STB PNG
     */
    private static void exportTexture(Texture2D texture, String path) {
        int texId = texture.getTextureId();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

        int width = texture.getWidth();
        int height = texture.getHeight();

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        if (!STBImageWrite.stbi_write_png(path, width, height, 4, buffer, width * 4)) {
            throw new RuntimeException("Failed to write texture PNG: " + path);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}
