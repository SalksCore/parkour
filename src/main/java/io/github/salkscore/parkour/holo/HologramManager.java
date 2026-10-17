package io.github.salkscore.parkour.holo;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public final class HologramManager implements Listener {

    private final ParkourPlugin plugin;
    private final Map<String, List<Hologram>> holograms = new HashMap<>();

    public HologramManager(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    public void rebuildAll() {
        clear();
        plugin.courses().all().forEach(this::rebuild);
    }

    public void rebuild(Course course) {
        remove(course.id());
        if (!plugin.settings().holograms()) return;
        List<Hologram> list = new CourseHolograms(plugin).create(course);
        list.forEach(Hologram::spawn);
        holograms.put(course.id(), list);
    }

    public void refresh(Course course) {
        holograms.getOrDefault(course.id(), List.of()).forEach(Hologram::update);
    }

    public void remove(String id) {
        List<Hologram> list = holograms.remove(id);
        if (list != null) list.forEach(Hologram::remove);
    }

    public void clear() {
        holograms.values().forEach(list -> list.forEach(Hologram::remove));
        holograms.clear();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World world = event.getWorld();
        int x = event.getChunk().getX();
        int z = event.getChunk().getZ();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (List<Hologram> list : holograms.values()) {
                list.stream().filter(holo -> holo.isIn(world, x, z)).forEach(Hologram::spawn);
            }
        });
    }
}
