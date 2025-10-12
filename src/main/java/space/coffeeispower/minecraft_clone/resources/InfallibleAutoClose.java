package space.coffeeispower.minecraft_clone.resources;

public interface InfallibleAutoClose extends AutoCloseable {
    @Override
    void close();
}
