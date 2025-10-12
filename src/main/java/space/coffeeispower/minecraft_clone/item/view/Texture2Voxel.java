package space.coffeeispower.minecraft_clone.item.view;

import org.joml.Vector4i;
import space.coffeeispower.minecraft_clone.opengl.model.Buffer;
import space.coffeeispower.minecraft_clone.opengl.model.BufferGroup;
import space.coffeeispower.minecraft_clone.opengl.texture.ImageData;

import java.util.ArrayList;
import java.util.List;

public class Texture2Voxel {

    private static void appendPixel(
            List<Double> vertices,
            List<Double> colorsList,
            boolean left, boolean right, boolean front, boolean back,
            int x,
            int z,
            Vector4i color
    ) {
        if (front) {
            appendFace(vertices, colorsList,
                    new double[]{


                            x + 1, 1, z + 1,
                            x, 1, z + 1,
                            x + 1, 0, z + 1,

                            x, 0, z + 1,
                            x + 1, 0, z + 1,
                            x, 1, z + 1,
                    }, color
            );
        }

        if (back) {
            appendFace(vertices, colorsList,
                    new double[]{

                            x + 1, 0, z,
                            x, 1, z,
                            x + 1, 1, z,

                            x, 1, z,
                            x + 1, 0, z,
                            x, 0, z,
                    }, color);
        }

        if (left) {
            appendFace(vertices, colorsList,
                    new double[]{
                            x, 0 + 1, z + 1,
                            x, 0 + 1, z,
                            x, 0, z,

                            x, 0, z,
                            x, 0, z + 1,
                            x, 0 + 1, z + 1
                    }, color);
        }

        if (right) {
            appendFace(vertices, colorsList,
                    new double[]{

                            x + 1, 1, z,
                            x + 1, 1, z + 1,
                            x + 1, 0, z,

                            x + 1, 0, z + 1,
                            x + 1, 0, z,
                            x + 1, 1, z + 1
                    }, color);
        }
        // Cima
        appendFace(vertices, colorsList,
                new double[]{
                        x, 1, z,
                        x, 1, z + 1,
                        x + 1, 1, z + 1,

                        x + 1, 1, z + 1,
                        x + 1, 1, z,
                        x, 1, z
                }, color);
        // Baixo
        appendFace(vertices, colorsList,
                new double[]{
                        x, 0, z + 1,
                        x, 0, z,
                        x + 1, 0, z + 1,

                        x + 1, 0, z,
                        x + 1, 0, z + 1,
                        x, 0, z
                }, color);
    }

    private static void appendFace(List<Double> vertices, List<Double> colors, double[] faceVertices, Vector4i color) {
        for (double p : faceVertices)
            vertices.add(p);
        for (int i = 0; i < faceVertices.length / 3; i++) {
            colors.add(color.x / 255.);
            colors.add(color.y / 255.);
            colors.add(color.z / 255.);
            colors.add(color.w / 255.);
        }
    }

    public static BufferGroup generateMeshForTexture(ImageData image) {
        int width = image.width();
        int height = image.height();

        var verticesList = new ArrayList<Double>();
        var colorsList = new ArrayList<Double>();


        // Percorrer todos os pixels
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                var color = image.getPixelColor(x, z);
                if (color.w < 255) continue; // ignora transparente

                // Descobrir que lados estão expostos
                boolean left = image.getPixelColor(x - 1, z).w < 255;
                boolean right = image.getPixelColor(x + 1, z).w < 255;
                boolean front = image.getPixelColor(x, z + 1).w < 255;
                boolean back = image.getPixelColor(x, z - 1).w < 255;
                appendPixel(verticesList, colorsList, left, right, front, back, x, z, color);
            }
        }

        double[] verts = verticesList.stream().mapToDouble(Double::doubleValue).toArray();
        double[] colors = colorsList.stream().mapToDouble(Double::doubleValue).toArray();
        return new BufferGroup(new Buffer(verts, 3), new Buffer(colors, 4));
    }

}
