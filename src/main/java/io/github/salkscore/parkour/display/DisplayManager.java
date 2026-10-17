package io.github.salkscore.parkour.display;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public final class DisplayManager implements Listener {

    private final ParkourPlugin plugin;
    private final Map<String, List<Visual>> visuals = new HashMap<>();

    public DisplayManager(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    public void rebuildAll() {
        clear();
        plugin.courses().all().forEach(this::rebuild);
    }

    public void rebuild(Course course) {
        remove(course.id());
        List<Visual> list = new CourseVisuals(plugin).create(course);
        list.forEach(this::spawn);
        visuals.put(course.id(), list);
        plugin.bridge().syncAll();
    }

    public void refresh(Course course) {
        visuals.getOrDefault(course.id(), List.of()).forEach(Visual::update);
        plugin.bridge().syncAll();
    }

    public void remove(String id) {
        List<Visual> list = visuals.remove(id);
        if (list != null) list.forEach(Visual::remove);
        plugin.bridge().syncAll();
    }

    public void hideFrom(Player player) {
        visuals.values().forEach(list -> list.forEach(v -> v.hideFrom(player, plugin)));
    }

    public void clear() {
        visuals.values().forEach(list -> list.forEach(Visual::remove));
        visuals.clear();
    }

    private void spawn(Visual visual) {
        if (!visual.spawn()) return;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (plugin.bridge().hasMod(player)) visual.hideFrom(player, plugin);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        int x = event.getChunk().getX();
        int z = event.getChunk().getZ();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (List<Visual> list : visuals.values()) {
                list.stream().filter(v -> v.isIn(world, x, z)).forEach(this::spawn);
            }
        });
    }
}
