package io.github.salkscore.parkour.holo;

import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

final class Hologram {

    private static final Color BACKGROUND = Color.fromARGB(80, 0, 0, 0);

    private final Location location;
    private final Supplier<Component> text;
    private TextDisplay entity;

    Hologram(Location location, Supplier<Component> text) {
        this.location = location;
        this.text = text;
    }

    boolean spawn() {
        if (entity != null && entity.isValid()) return false;
        World world = location.getWorld();
        if (world == null || !world.isChunkLoaded(chunkX(), chunkZ())) return false;
        entity = world.spawn(location, TextDisplay.class, this::setup);
        return true;
    }

    TextDisplay entity() {
        return entity != null && entity.isValid() ? entity : null;
    }

    private void setup(TextDisplay display) {
        display.setPersistent(false);
        display.setBillboard(Display.Billboard.CENTER);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setBackgroundColor(BACKGROUND);
        display.setShadowed(true);
        display.setLineWidth(300);
        display.text(text.get());
    }

    void update() {
        if (entity != null && entity.isValid()) entity.text(text.get());
    }

    void remove() {
        if (entity != null) entity.remove();
        entity = null;
    }

    boolean isIn(World world, int x, int z) {
        return world.equals(location.getWorld()) && chunkX() == x && chunkZ() == z;
    }

    private int chunkX() {
        return location.getBlockX() >> 4;
    }

    private int chunkZ() {
        return location.getBlockZ() >> 4;
    }
}
