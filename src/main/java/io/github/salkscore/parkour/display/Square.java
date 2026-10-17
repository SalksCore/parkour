package io.github.salkscore.parkour.display;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

final class Square implements Visual {

    private static final float HEIGHT = 0.02f;
    private static final float THICK = 0.06f;

    private final Location corner;
    private final BlockData block;
    private final List<BlockDisplay> bars = new ArrayList<>();

    Square(Location point, Material type) {
        this.corner = new Location(point.getWorld(), point.getBlockX(), point.getY() + 0.005,
                point.getBlockZ());
        this.block = type.createBlockData();
    }

    @Override
    public boolean spawn() {
        if (!bars.isEmpty() && bars.getFirst().isValid()) return false;
        World world = corner.getWorld();
        if (world == null || !corner.isChunkLoaded()) return false;

        bars.clear();
        float inner = 1 - 2 * THICK;
        bar(world, 0, 0, 1, THICK);
        bar(world, 0, 1 - THICK, 1, THICK);
        bar(world, 0, THICK, THICK, inner);
        bar(world, 1 - THICK, THICK, THICK, inner);
        return true;
    }

    private void bar(World world, float x, float z, float width, float depth) {
        Transformation transform = new Transformation(new Vector3f(x, 0, z), new AxisAngle4f(),
                new Vector3f(width, HEIGHT, depth), new AxisAngle4f());
        bars.add(world.spawn(corner, BlockDisplay.class, display -> {
            display.setPersistent(false);
            display.setBlock(block);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setTransformation(transform);
        }));
    }

    @Override
    public void update() {
    }

    @Override
    public void remove() {
        bars.forEach(BlockDisplay::remove);
        bars.clear();
    }

    @Override
    public boolean isIn(World world, int chunkX, int chunkZ) {
        return world.equals(corner.getWorld())
                && corner.getBlockX() >> 4 == chunkX
                && corner.getBlockZ() >> 4 == chunkZ;
    }
}
