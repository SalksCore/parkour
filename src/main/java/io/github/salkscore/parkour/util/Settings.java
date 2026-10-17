package io.github.salkscore.parkour.util;

import io.github.salkscore.parkour.run.RunItem;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public record Settings(
        Squares squares,
        GameMode gameMode,
        boolean noDamage,
        boolean noHunger,
        boolean resetOnWater,
        boolean resetOnLava,
        Map<RunItem, ItemSlot> items,
        boolean bossbar,
        BossBar.Color barColor,
        boolean holograms,
        double holoHeight,
        int boardSize) {

    public record Squares(Material start, Material checkpoint, Material finish) {
    }

    public record ItemSlot(Material material, int slot) {
    }

    public static Settings load(FileConfiguration c) {
        Squares squares = new Squares(
                material(c, "squares.start", Material.LIME_CONCRETE),
                material(c, "squares.checkpoint", Material.YELLOW_CONCRETE),
                material(c, "squares.finish", Material.RED_CONCRETE));

        Map<RunItem, ItemSlot> items = new EnumMap<>(RunItem.class);
        for (RunItem item : RunItem.values()) {
            String path = "items." + item.key();
            if (!c.getBoolean(path + ".enabled", true)) continue;
            Material type = material(c, path + ".material", Material.PAPER);
            items.put(item, new ItemSlot(type, c.getInt(path + ".slot", item.ordinal())));
        }

        return new Settings(
                squares,
                parse(GameMode.class, c.getString("run.gamemode"), GameMode.ADVENTURE),
                c.getBoolean("run.no-damage", true),
                c.getBoolean("run.no-hunger", true),
                c.getBoolean("run.reset-on-water", true),
                c.getBoolean("run.reset-on-lava", true),
                items,
                c.getBoolean("hud.bossbar", true),
                parse(BossBar.Color.class, c.getString("hud.bossbar-color"), BossBar.Color.GREEN),
                c.getBoolean("holograms.enabled", true),
                c.getDouble("holograms.height", 1.4),
                Math.max(1, c.getInt("holograms.board-size", 10)));
    }

    private static Material material(FileConfiguration c, String path, Material fallback) {
        Material type = Material.matchMaterial(c.getString(path, ""));
        return type != null && type.isBlock() ? type : fallback;
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String raw, E fallback) {
        if (raw == null) return fallback;
        try {
            return Enum.valueOf(type, raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
