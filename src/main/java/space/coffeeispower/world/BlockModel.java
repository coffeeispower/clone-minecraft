package space.coffeeispower.world;

import space.coffeeispower.opengl.texture.TextureAtlas;

import java.util.List;

public record BlockModel(String topPath, String bottomPath, String frontPath, String rightPath, String leftPath,
                         String backPath) {
    public BlockModel(String all) {
        this(all, all, all, all, all, all);
    }

    public BlockModel(String top, String bottom, String side) {
        this(top, bottom, side, side, side, side);
    }


    /**
     * Adiciona as faces selecionadas aos buffers de vértices e UVs.
     *
     * @param vertices lista onde os vértices serão adicionados
     * @param uvs      lista onde as coordenadas UV serão adicionadas
     * @param atlas    o atlas de texturas (para obter coordenadas UV)
     * @param top      se deve gerar a face de cima
     * @param bottom   se deve gerar a face de baixo
     * @param left     se deve gerar a face esquerda
     * @param right    se deve gerar a face direita
     * @param front    se deve gerar a face da frente
     * @param back     se deve gerar a face de trás
     */
    public void appendFaces(List<Double> vertices, List<Double> uvs, TextureAtlas atlas,
                            boolean top, boolean bottom, boolean left, boolean right, boolean front, boolean back,
                            int x,
                            int y,
                            int z
    ) {

        if (front) {
            var uv = atlas.getUV(frontPath);
            appendFace(vertices, uvs,
                    new double[]{

                            x + 0.5, y + 0.5, z + 0.5,
                            x - 0.5, y + 0.5, z + 0.5,
                            x + 0.5, y - 0.5, z + 0.5,

                            x - 0.5, y - 0.5, z + 0.5,
                            x + 0.5, y - 0.5, z + 0.5,
                            x - 0.5, y + 0.5, z + 0.5,
                    },
                    new double[]{
                            uv.right(), uv.top(),
                            uv.left(), uv.top(),
                            uv.right(), uv.bottom(),

                            uv.right(), uv.bottom(),
                            uv.left(), uv.bottom(),
                            uv.right(), uv.top()

                    }
            );
        }

        if (back) {
            var uv = atlas.getUV(backPath);
            appendFace(vertices, uvs,
                    new double[]{

                            x + 0.5, y - 0.5, z - 0.5,
                            x - 0.5, y + 0.5, z - 0.5,
                            x + 0.5, y + 0.5, z - 0.5,

                            x - 0.5, y + 0.5, z - 0.5,
                            x + 0.5, y - 0.5, z - 0.5,
                            x - 0.5, y - 0.5, z - 0.5,
                    }, new double[]{
                            uv.right(), uv.bottom(),
                            uv.left(), uv.top(),
                            uv.right(), uv.top(),
                            uv.left(), uv.top(),
                            uv.right(), uv.bottom(),
                            uv.left(), uv.bottom()
                    });
        }

        if (left) {
            var uv = atlas.getUV(leftPath);
            appendFace(vertices, uvs,
                    new double[]{
                            x - 0.5, y + 0.5, z + 0.5,
                            x - 0.5, y + 0.5, z - 0.5,
                            x - 0.5, y - 0.5, z - 0.5,

                            x - 0.5, y - 0.5, z - 0.5,
                            x - 0.5, y - 0.5, z + 0.5,
                            x - 0.5, y + 0.5, z + 0.5
                    }, new double[]{
                            uv.right(), uv.top(),
                            uv.left(), uv.top(),
                            uv.left(), uv.bottom(),

                            uv.left(), uv.bottom(),
                            uv.right(), uv.bottom(),
                            uv.right(), uv.top(),

                    });
        }

        if (right) {
            var uv = atlas.getUV(rightPath);
            appendFace(vertices, uvs,
                    new double[]{

                            x + 0.5, y + 0.5, z - 0.5,
                            x + 0.5, y + 0.5, z + 0.5,
                            x + 0.5, y - 0.5, z - 0.5,

                            x + 0.5, y - 0.5, z + 0.5,
                            x + 0.5, y - 0.5, z - 0.5,
                            x + 0.5, y + 0.5, z + 0.5
                    }, new double[]{

                            uv.right(), uv.top(),
                            uv.left(), uv.top(),
                            uv.right(), uv.bottom(),

                            uv.left(), uv.bottom(),
                            uv.right(), uv.bottom(),
                            uv.left(), uv.top(),
                    });
        }

        if (top) {
            var uv = atlas.getUV(topPath);
            appendFace(vertices, uvs,
                    new double[]{
                            x - 0.5, y + 0.5, z - 0.5,
                            x - 0.5, y + 0.5, z + 0.5,
                            x + 0.5, y + 0.5, z + 0.5,

                            x + 0.5, y + 0.5, z + 0.5,
                            x + 0.5, y + 0.5, z - 0.5,
                            x - 0.5, y + 0.5, z - 0.5
                    }, new double[]{
                            uv.left(), uv.top(),
                            uv.left(), uv.bottom(),
                            uv.right(), uv.bottom(),

                            uv.right(), uv.bottom(),
                            uv.right(), uv.top(),
                            uv.left(), uv.top(),
                    });
        }

        if (bottom) {
            var uv = atlas.getUV(bottomPath);
            appendFace(vertices, uvs,
                    new double[]{
                            x - 0.5, y - 0.5, z + 0.5,
                            x - 0.5, y - 0.5, z - 0.5,
                            x + 0.5, y - 0.5, z + 0.5,

                            x + 0.5, y - 0.5, z - 0.5,
                            x + 0.5, y - 0.5, z + 0.5,
                            x - 0.5, y - 0.5, z - 0.5
                    }, new double[]{

                            uv.left(), uv.bottom(),
                            uv.left(), uv.top(),
                            uv.right(), uv.bottom(),

                            uv.right(), uv.top(),
                            uv.right(), uv.bottom(),
                            uv.left(), uv.top(),
                    });
        }
    }

    private void appendFace(List<Double> vertices, List<Double> uvs, double[] faceVertices, double[] srcUv) {
        for (double v : faceVertices)
            vertices.add(v);

        for (double d : srcUv)
            uvs.add(d);
    }
}
