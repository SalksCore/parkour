package io.github.salkscore.parkour.course;

import java.util.UUID;
import org.bukkit.Location;

public record BlockKey(UUID world, int x, int y, int z) {

    public static BlockKey of(Location loc) {
        return new BlockKey(loc.getWorld().getUID(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
