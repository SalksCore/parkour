package io.github.salkscore.parkour.display;

import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;

final class Hologram implements Visual {

    private static final Color BACKGROUND = Color.fromARGB(80, 0, 0, 0);

    private final Location location;
    private final Supplier<Component> text;
    // le mod affiche sa propre version, avec le record du joueur
    private final boolean drawnByMod;
    private TextDisplay entity;

    Hologram(Location location, Supplier<Component> text, boolean drawnByMod) {
        this.location = location;
        this.text = text;
        this.drawnByMod = drawnByMod;
    }

    @Override
    public boolean spawn() {
        if (entity != null && entity.isValid()) return false;
        World world = location.getWorld();
        if (world == null || !location.isChunkLoaded()) return false;
        entity = world.spawn(location, TextDisplay.class, this::setup);
        return true;
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

    @Override
    public void update() {
        if (entity != null && entity.isValid()) entity.text(text.get());
    }

    @Override
    public void remove() {
        if (entity != null) entity.remove();
        entity = null;
    }

    @Override
    public boolean isIn(World world, int chunkX, int chunkZ) {
        return world.equals(location.getWorld())
                && location.getBlockX() >> 4 == chunkX
                && location.getBlockZ() >> 4 == chunkZ;
    }

    @Override
    public void hideFrom(Player player, Plugin plugin) {
        if (drawnByMod && entity != null && entity.isValid()) player.hideEntity(plugin, entity);
    }
}
