package space.coffeeispower.world;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import space.coffeeispower.opengl.model.Buffer;
import space.coffeeispower.opengl.model.BufferGroup;
import space.coffeeispower.opengl.texture.TextureAtlas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World {
    private final HashMap<Vector2i, Chunk> chunks = new HashMap<>();
    private final HashMap<Vector2i, ChunkLoadState> chunkLoadStates = new HashMap<>();

    private sealed interface ChunkLoadState {}

    private record GeneratingTerrain(Thread thread) implements ChunkLoadState {}
    private record GeneratedTerrain(Chunk chunk) implements ChunkLoadState {}
    private record GeneratingMesh(Thread thread) implements ChunkLoadState {}
    private record GeneratedMesh(ChunkMeshGenerator.ChunkMesh mesh) implements ChunkLoadState {}
    private record MeshUploaded(BufferGroup model) implements ChunkLoadState {}
    private int seed;
    public World(int seed) {
        this.seed = seed;
    }
    public static final int RENDER_DISTANCE = 5;

    public void loadChunksAround(double playerX, double playerZ, TextureAtlas atlas) {
        var playerChunkX = (int) Math.floor(playerX / Chunk.CHUNK_WIDTH);
        var playerChunkZ = (int) Math.floor(playerZ / Chunk.CHUNK_WIDTH);
        var currentChunkPos = new Vector2i();
        for (int x = playerChunkX-3; x < playerChunkX+3; x++) {
            for (int z = playerChunkZ-3; z < playerChunkZ+3; z++) {
                currentChunkPos.x = x;
                currentChunkPos.y = z;
                if(RENDER_DISTANCE*RENDER_DISTANCE < currentChunkPos.distanceSquared(playerChunkX, playerChunkZ)) {
                    continue;
                }
                Chunk chunk = chunks.get(currentChunkPos);
                ChunkLoadState stage = chunkLoadStates.get(currentChunkPos);
                switch (stage) {
                    case null -> {
                        if(chunk != null) {
                            chunkLoadStates.put(new Vector2i(currentChunkPos), new GeneratedTerrain(chunk));
                            continue;
                        }
                        var thread = createChunkGeneratorThread(currentChunkPos);
                        chunkLoadStates.put(new Vector2i(currentChunkPos), new GeneratingTerrain(thread));
                        thread.start();
                    }
                    case GeneratingTerrain ignored -> {

                    }
                    case GeneratedTerrain terrain -> {
                        var chunkPos = new Vector2i(currentChunkPos);
                        var thread = new Thread(() -> {
                            chunkLoadStates.put(chunkPos, new GeneratedMesh(ChunkMeshGenerator.generateMeshForChunk(terrain.chunk, atlas)));
                        });
                        chunkLoadStates.put(chunkPos, new GeneratingMesh(thread));
                        thread.start();
                    }
                    case GeneratingMesh ignored -> {

                    }
                    case GeneratedMesh mesh -> {
                        var model = new BufferGroup();
                        model.addBuffer(new Buffer(mesh.mesh().vertices(), 3));
                        model.addBuffer(new Buffer(mesh.mesh().uv(), 2));
                        chunkLoadStates.put(new Vector2i(currentChunkPos), new MeshUploaded(model));
                    }
                    case MeshUploaded ignored -> {}
                }
            }
        }
    }

    @NotNull
    private Thread createChunkGeneratorThread(Vector2i currentChunkPos) {
        var chunkPos = new Vector2i(currentChunkPos);
        return new Thread(() -> {
            Chunk newChunk = new Chunk(chunkPos);
            WorldGen.generateChunk(newChunk, seed);
            chunkLoadStates.put(chunkPos, new GeneratedTerrain(newChunk));
            chunks.put(chunkPos, newChunk);
        });
    }

    @NotNull
    public Stream<LoadedChunk> getLoadedChunks() {
        return chunkLoadStates
                .entrySet()
                .stream()
                .filter((e) -> e.getValue() instanceof MeshUploaded && chunks.containsKey(e.getKey()))
                .map((e) -> new LoadedChunk(chunks.get(e.getKey()), ((MeshUploaded) e.getValue()).model()));
    }
    public Iterable<LoadedChunk> getLoadedChunksIterable(){
        return getLoadedChunks()::iterator;
    }
    public void garbageCollectChunks(double playerX, double playerZ) {
        var playerChunkX = (int) Math.floor(playerX / Chunk.CHUNK_WIDTH);
        var playerChunkZ = (int) Math.floor(playerZ / Chunk.CHUNK_WIDTH);
        var chunksThatShouldBeUnloaded = getLoadedChunks().filter((c) -> RENDER_DISTANCE*RENDER_DISTANCE < c.chunk().getPosition().distanceSquared(playerChunkX, playerChunkZ)).collect(Collectors.toCollection(ArrayList::new));
        for(LoadedChunk c: chunksThatShouldBeUnloaded) {
            c.model().close();
            chunkLoadStates.remove(c.chunk().getPosition());
        }
    }
    public record LoadedChunk(Chunk chunk, BufferGroup model) {}
}
