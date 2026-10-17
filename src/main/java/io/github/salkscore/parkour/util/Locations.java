package io.github.salkscore.parkour.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class Locations {

    private Locations() {
    }

    public static Location snap(Location loc) {
        float yaw = Math.round(loc.getYaw() / 45f) * 45f;
        return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getY(),
                loc.getBlockZ() + 0.5, yaw, 0f);
    }

    public static String serialize(Location loc) {
        return String.join(";", loc.getWorld().getName(),
                String.valueOf(loc.getX()), String.valueOf(loc.getY()), String.valueOf(loc.getZ()),
                String.valueOf(loc.getYaw()), String.valueOf(loc.getPitch()));
    }

    public static Location parse(String raw) {
        String[] parts = raw.split(";");
        if (parts.length < 4) throw new IllegalStateException("Position invalide : " + raw);

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) throw new IllegalStateException("Monde introuvable : " + parts[0]);

        Location loc = new Location(world, Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
        if (parts.length >= 6) {
            loc.setYaw(Float.parseFloat(parts[4]));
            loc.setPitch(Float.parseFloat(parts[5]));
        }
        return loc;
    }

    public static String pretty(Location loc) {
        return "%s %d, %d, %d".formatted(loc.getWorld().getName(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
