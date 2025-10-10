package space.coffeeispower.entity.test;

import org.joml.Vector3d;
import org.joml.Vector4f;
import space.coffeeispower.entity.EntityRenderer;
import space.coffeeispower.entity.FallingCollidingEntity;
import space.coffeeispower.world.World;

public class TestEntity extends FallingCollidingEntity<TestEntity> {
    Vector4f color;
    TestEntityRenderer renderer;
    public TestEntity(Vector3d position, Vector4f color, World world) {
        super(position, new Vector3d(0.5, 2.0, 0.5), world);
        this.color = color;
        this.renderer = new TestEntityRenderer(this);
    }


    @Override
    public EntityRenderer<TestEntity> getRenderer() {
        return this.renderer;
    }
}
