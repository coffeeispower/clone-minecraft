package space.coffeeispower.minecraft_clone.world;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.jspecify.annotations.Nullable;
import space.coffeeispower.minecraft_clone.entity.Entity;
import space.coffeeispower.minecraft_clone.opengl.model.Buffer;
import space.coffeeispower.minecraft_clone.opengl.model.BufferGroup;
import space.coffeeispower.minecraft_clone.world.block.BlockType;
import space.coffeeispower.minecraft_clone.world.block.breaking.BlockBreakState;
import space.coffeeispower.minecraft_clone.world.chunk.Chunk;
import space.coffeeispower.minecraft_clone.world.chunk.ChunkMeshGenerator;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World implements Closeable {
    private final HashMap<Vector2i, Chunk> chunks = new HashMap<>();
    private final HashMap<Vector2i, ChunkLoadState> chunkLoadStates = new HashMap<>();
    private final ArrayList<Entity<?>> entities = new ArrayList<>();

    private sealed interface ChunkLoadState {
    }

    private record GeneratingTerrain() implements ChunkLoadState {
    }

    private record GeneratedTerrain(Chunk chunk) implements ChunkLoadState {
    }

    private record GeneratingMesh() implements ChunkLoadState {
    }

    private record RegeneratingMesh(BufferGroup model) implements ChunkLoadState {
    }

    private record GeneratedMesh(ChunkMeshGenerator.ChunkMesh mesh) implements ChunkLoadState {
    }

    private record MeshUploaded(BufferGroup model) implements ChunkLoadState {
    }

    private record NeedsRemeshing(BufferGroup model) implements ChunkLoadState {
    }

    private record NeedsReupload(ChunkMeshGenerator.ChunkMesh mesh, BufferGroup model) implements ChunkLoadState {
    }

    private final int seed;

    public World(int seed) {
        this.seed = seed;
    }

    public static final int RENDER_DISTANCE = 8;

    public void loadChunksAround(double playerX, double playerZ) {
        var playerChunkX = (int) Math.floor(playerX / Chunk.CHUNK_WIDTH);
        var playerChunkZ = (int) Math.floor(playerZ / Chunk.CHUNK_WIDTH);
        var currentChunkPos = new Vector2i();
        for (int x = playerChunkX - RENDER_DISTANCE; x < playerChunkX + RENDER_DISTANCE; x++) {
            for (int z = playerChunkZ - RENDER_DISTANCE; z < playerChunkZ + RENDER_DISTANCE; z++) {
                currentChunkPos.x = x;
                currentChunkPos.y = z;
                if (RENDER_DISTANCE * RENDER_DISTANCE < currentChunkPos.distanceSquared(playerChunkX, playerChunkZ)) {
                    continue;
                }
                Chunk chunk = chunks.get(currentChunkPos);
                ChunkLoadState stage = chunkLoadStates.get(currentChunkPos);
                switch (stage) {
                    case null -> {
                        if (chunk != null) {
                            synchronized (chunkLoadStates) {
                                chunkLoadStates.put(new Vector2i(currentChunkPos), new GeneratedTerrain(chunk));
                            }
                            continue;
                        }
                        var thread = createChunkGeneratorThread(currentChunkPos);
                        synchronized (chunkLoadStates) {
                            chunkLoadStates.put(new Vector2i(currentChunkPos), new GeneratingTerrain());
                        }
                        thread.start();
                    }
                    case GeneratingTerrain ignored -> {

                    }
                    case GeneratedTerrain terrain -> {
                        var chunkPos = new Vector2i(currentChunkPos);
                        var thread = new Thread(() -> {
                            synchronized (chunkLoadStates) {
                                chunkLoadStates.put(chunkPos, new GeneratedMesh(ChunkMeshGenerator.generateMeshForChunk(terrain.chunk)));
                            }
                        });
                        synchronized (chunkLoadStates) {
                            chunkLoadStates.put(chunkPos, new GeneratingMesh());
                        }
                        thread.start();
                    }
                    case NeedsRemeshing needsRemeshing -> {
                        var chunkPos = new Vector2i(currentChunkPos);
                        var thread = new Thread(() -> {
                            synchronized (chunkLoadStates) {
                                chunkLoadStates.put(chunkPos, new NeedsReupload(ChunkMeshGenerator.generateMeshForChunk(chunk), needsRemeshing.model()));
                            }
                        });
                        synchronized (chunkLoadStates) {
                            chunkLoadStates.put(chunkPos, new RegeneratingMesh(needsRemeshing.model()));
                        }
                        thread.start();
                    }
                    case GeneratingMesh ignored -> {

                    }
                    case RegeneratingMesh ignored -> {

                    }
                    case GeneratedMesh mesh -> {
                        var model = new BufferGroup();
                        model.addBuffer(new Buffer(mesh.mesh().vertices(), 3));
                        model.addBuffer(new Buffer(mesh.mesh().uv(), 2));
                        synchronized (chunkLoadStates) {
                            chunkLoadStates.put(new Vector2i(currentChunkPos), new MeshUploaded(model));
                        }
                    }
                    case NeedsReupload mesh -> {
                        var model = mesh.model();
                        model.getBuffer(0).updateBuffer(mesh.mesh().vertices());
                        model.getBuffer(1).updateBuffer(mesh.mesh().uv());
                        synchronized (chunkLoadStates) {
                            chunkLoadStates.put(new Vector2i(currentChunkPos), new MeshUploaded(model));
                        }
                    }
                    case MeshUploaded ignored -> {
                    }
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
            synchronized (chunks) {
                synchronized (chunkLoadStates) {
                    chunkLoadStates.put(chunkPos, new GeneratedTerrain(newChunk));
                    chunks.put(chunkPos, newChunk);
                }
            }
        });
    }

    @SuppressWarnings("resource")
    @NotNull
    public Stream<LoadedChunk> getLoadedChunks() {
        synchronized (chunkLoadStates) {
            return chunkLoadStates
                    .entrySet()
                    .stream()
                    .map((e) -> {
                        var chunk = chunks.get(e.getKey());
                        if (chunk == null) {
                            return null;
                        }
                        var model = switch (e.getValue()) {
                            case NeedsRemeshing needsRemesh -> needsRemesh.model();
                            case MeshUploaded uploaded -> uploaded.model();
                            case NeedsReupload needsReupload -> needsReupload.model();
                            case RegeneratingMesh regenerating -> regenerating.model();

                            default -> null;
                        };
                        if (model == null) {
                            return null;
                        }
                        return new LoadedChunk(chunk, model);
                    }).filter(Objects::nonNull);
        }
    }

    public Iterable<LoadedChunk> getLoadedChunksIterable() {
        return getLoadedChunks()::iterator;
    }

    public void garbageCollectChunks(double playerX, double playerZ) {
        var playerChunkX = (int) Math.floor(playerX / Chunk.CHUNK_WIDTH);
        var playerChunkZ = (int) Math.floor(playerZ / Chunk.CHUNK_WIDTH);
        ArrayList<World.LoadedChunk> chunksThatShouldBeUnloaded;
        synchronized (chunkLoadStates) {
            chunksThatShouldBeUnloaded = getLoadedChunks().filter((c) -> RENDER_DISTANCE * RENDER_DISTANCE < c.chunk().getPosition().distanceSquared(playerChunkX, playerChunkZ)).collect(Collectors.toCollection(ArrayList::new));
        }
        for (LoadedChunk c : chunksThatShouldBeUnloaded) {
            synchronized (chunkLoadStates) {
                chunkLoadStates.remove(c.chunk().getPosition());
            }
            c.model().close();
        }
    }

    public record LoadedChunk(Chunk chunk, BufferGroup model) {
    }

    public Chunk getChunkAtBlock(int x, int z) {
        int chunkX = Math.floorDiv(x, Chunk.CHUNK_WIDTH);
        int chunkZ = Math.floorDiv(z, Chunk.CHUNK_WIDTH);
        return chunks.get(new Vector2i(chunkX, chunkZ));
    }

    public Chunk getChunkAtBlock(Vector2i position) {
        return getChunkAtBlock(position.x, position.y);
    }

    public Chunk getChunkAt(Vector2i position) {
        return chunks.get(position);
    }

    public boolean isChunkLoaded(Vector2i position) {
        synchronized (chunkLoadStates) {
            return chunkLoadStates.get(position) instanceof MeshUploaded;
        }
    }

    public boolean isChunkLoaded(Chunk chunk) {
        synchronized (chunkLoadStates) {
            return chunkLoadStates.get(chunk.getPosition()) instanceof MeshUploaded;
        }
    }

    public boolean isSolid(int x, int y, int z) {
        return getBlock(x, y, z).isSolid();
    }

    public BlockType getBlock(int x, int y, int z) {
        if (y > 256 || y < 0) {
            return BlockType.Air;
        }
        var chunk = getChunkAtBlock(x, z);
        if (chunk == null) {
            return BlockType.Air;
        }

        int localX = Math.floorMod(x, Chunk.CHUNK_WIDTH);
        int localZ = Math.floorMod(z, Chunk.CHUNK_WIDTH);
        return chunk.getBlockAt(localX, y, localZ);
    }

    public BlockType getBlock(Vector3i position) {
        return getBlock(position.x, position.y, position.z);
    }
    public void setBlock(Vector3i position, BlockType block) {
        setBlock(position.x, position.y, position.z, block);
    }
    public void setBlock(int x, int y, int z, BlockType block) {
        if (y > 256 || y < 0) {
            return;
        }
        var chunk = getChunkAtBlock(x, z);
        if (chunk == null) {
            return;
        }

        int localX = Math.floorMod(x, Chunk.CHUNK_WIDTH);
        int localZ = Math.floorMod(z, Chunk.CHUNK_WIDTH);
        synchronized (chunk) {
            chunk.setBlockAt(localX, y, localZ, block);
        }
        synchronized (chunkLoadStates) {
            if (chunkLoadStates.get(chunk.getPosition()) instanceof MeshUploaded(BufferGroup model)) {
                chunkLoadStates.put(chunk.getPosition(), new NeedsRemeshing(model));
            }
        }
    }
    @Override
    public void close() {
        for (var e: chunkLoadStates.entrySet()) {
            var mesh = e.getValue();
            if (mesh instanceof MeshUploaded(var model)) {
                model.close();
                e.setValue(null);
            }
        }
    }

    public void updateEntities(double deltaTime) {
        for (Entity<?> entity : entities) {
            entity.update(deltaTime);
        }
    }

    public ArrayList<Entity<?>> getEntities() {
        return entities;
    }

    public <T extends Entity<T>> T spawnEntity(T entity) {
        entities.add(entity);
        return entity;
    }

    private final HashMap<Vector3i, BlockBreakState> breakingStates = new HashMap<>(1);

    public void updateBreakingStates(double deltaTime) {
        for (Map.Entry<Vector3i, BlockBreakState> entry : breakingStates.entrySet()) {
            Vector3i pos = entry.getKey();
            BlockBreakState state = entry.getValue();

            // atualiza o progresso
            boolean finished = state.update(deltaTime);

            if (finished) {
                setBlock(pos, BlockType.Air);
                breakingStates.remove(pos);
            }
        }
    }

    public void startBreakingBlock(Entity<?> entity, Vector3i blockPosition) {
        breakingStates.putIfAbsent(blockPosition, new BlockBreakState(entity, 1.0));
    }

    public void interruptBlockBreaking(Vector3i blockPosition, @Nullable Entity<?> entity) {
        if (entity != null && breakingStates.containsKey(blockPosition) && breakingStates.get(blockPosition).getBreaker() != entity) {
            return;
        }
        breakingStates.remove(blockPosition);
    }

    public boolean isBlockBeingBroken(Vector3i blockPosition, @Nullable Entity<?> entity) {
        return breakingStates.containsKey(blockPosition) && (entity == null || breakingStates.get(blockPosition).getBreaker() == entity);
    }

    public double getBreakingProgress(Vector3i blockPosition) {
        var p = breakingStates.get(blockPosition);
        return p == null ? 0 : p.getProgress();
    }
}