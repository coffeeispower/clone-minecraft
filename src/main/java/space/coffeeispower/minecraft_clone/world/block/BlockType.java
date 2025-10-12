package space.coffeeispower.minecraft_clone.world.block;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BlockType {
    Air(null, false),
    Bedrock(new BlockModel("/textures/blocks/bedrock/bedrock.png")),
    Dirt(new BlockModel("/textures/blocks/dirt/dirt.png")),
    Grass(new BlockModel("/textures/blocks/grass/top.png", "/textures/blocks/grass/bottom.png", "/textures/blocks/grass/side.png")),
    Stone(new BlockModel("/textures/blocks/stone/stone.png")),
    ;
    @Nullable
    private final BlockModel model;
    private boolean isSolid = true;

    BlockType(@Nullable BlockModel blockModel) {
        model = blockModel;
    }

    BlockType(@Nullable BlockModel blockModel, boolean isSolid) {
        model = blockModel;
        this.isSolid = isSolid;
    }

    public static Set<String> getAllTexturesPaths() {
        return Arrays.stream(values()).flatMap((b) -> {
            if (b.model() == null) return Stream.empty();
            return Stream.of(
                    b.model().topPath(),
                    b.model().bottomPath(),
                    b.model().frontPath(),
                    b.model().rightPath(),
                    b.model().leftPath(),
                    b.model().backPath()
            );
        }).collect(Collectors.toSet());
    }

    @Nullable
    public BlockModel model() {
        return model;
    }

    public boolean isSolid() {
        return isSolid;
    }
}
