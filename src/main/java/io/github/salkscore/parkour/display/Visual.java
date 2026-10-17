package io.github.salkscore.parkour.display;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

interface Visual {

    boolean spawn();

    void update();

    void remove();

    boolean isIn(World world, int chunkX, int chunkZ);

    default void hideFrom(Player player, Plugin plugin) {
    }
}
