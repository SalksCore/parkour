package io.github.salkscore.parkour.holo;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public final class PointParticles implements Runnable {

    private static final Particle.DustOptions START = dust(0x22C55E);
    private static final Particle.DustOptions CHECKPOINT = dust(0xFACC15);
    private static final Particle.DustOptions FINISH = dust(0xEF4444);

    private final ParkourPlugin plugin;
    private int tick;

    public PointParticles(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.settings().particles()) return;
        tick++;
        for (Course course : plugin.courses().all()) {
            ring(course.start(), START);
            course.checkpoints().forEach(loc -> ring(loc, CHECKPOINT));
            ring(course.finish(), FINISH);
        }
    }

    private void ring(Location loc, Particle.DustOptions dust) {
        if (loc == null) return;
        World world = loc.getWorld();
        if (world == null || !loc.isChunkLoaded()) return;

        for (int i = 0; i < 3; i++) {
            double angle = tick * 0.3 + i * (Math.PI * 2 / 3);
            double x = loc.getBlockX() + 0.5 + Math.cos(angle) * 0.5;
            double z = loc.getBlockZ() + 0.5 + Math.sin(angle) * 0.5;
            world.spawnParticle(Particle.DUST, x, loc.getBlockY() + 0.2, z, 1, 0, 0, 0, 0, dust);
        }
    }

    private static Particle.DustOptions dust(int rgb) {
        return new Particle.DustOptions(Color.fromRGB(rgb), 0.9f);
    }
}
