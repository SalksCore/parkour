package io.github.salkscore.parkour.course;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class Plates {

    private Plates() {
    }

    public static void place(Location loc, Material type) {
        Block block = loc.getBlock();
        if (block.getType().isAir()) block.setType(type);
    }

    public static void remove(Location loc, Material type) {
        if (loc == null) return;
        Block block = loc.getBlock();
        if (block.getType() == type) block.setType(Material.AIR);
    }
}
