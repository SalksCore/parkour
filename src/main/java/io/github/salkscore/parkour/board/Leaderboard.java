package io.github.salkscore.parkour.board;

import io.github.salkscore.parkour.ParkourPlugin;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class Leaderboard {

    public record Entry(UUID player, String name, long time) {
    }

    public record Result(long time, Long previous, int rank) {

        public boolean improved() {
            return previous == null || time < previous;
        }
    }

    private static final Comparator<Entry> ORDER = Comparator.comparingLong(Entry::time)
            .thenComparing(Entry::name);

    private final ParkourPlugin plugin;
    private final File file;
    private final Map<String, Map<UUID, Entry>> times = new HashMap<>();
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public Leaderboard(ParkourPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "times.yml");
    }

    public void load() {
        times.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String course : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(course);
            if (section == null) continue;

            Map<UUID, Entry> entries = times.computeIfAbsent(course, k -> new HashMap<>());
            for (String key : section.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    String name = section.getString(key + ".name", "?");
                    entries.put(id, new Entry(id, name, section.getLong(key + ".time")));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Entrée ignorée dans times.yml : " + key);
                }
            }
        }
    }

    public Result submit(String course, Player player, long time) {
        UUID id = player.getUniqueId();
        Map<UUID, Entry> entries = times.computeIfAbsent(course, k -> new HashMap<>());
        Entry old = entries.get(id);
        if (old == null || time < old.time()) {
            entries.put(id, new Entry(id, player.getName(), time));
            save();
        }
        return new Result(time, old == null ? null : old.time(), rank(course, id));
    }

    public List<Entry> top(String course, int limit) {
        return sorted(course).stream().limit(limit).toList();
    }

    public Entry best(String course, UUID id) {
        return times.getOrDefault(course, Map.of()).get(id);
    }

    public int rank(String course, UUID id) {
        List<Entry> sorted = sorted(course);
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).player().equals(id)) return i + 1;
        }
        return 0;
    }

    public List<String> names(String course) {
        return times.getOrDefault(course, Map.of()).values().stream().map(Entry::name).toList();
    }

    public void reset(String course) {
        if (times.remove(course) != null) save();
    }

    public boolean reset(String course, String name) {
        Map<UUID, Entry> entries = times.get(course);
        if (entries == null || !entries.values().removeIf(e -> e.name().equalsIgnoreCase(name))) {
            return false;
        }
        save();
        return true;
    }

    private List<Entry> sorted(String course) {
        return times.getOrDefault(course, Map.of()).values().stream().sorted(ORDER).toList();
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        times.forEach((course, entries) -> entries.values().forEach(entry -> {
            String path = course + "." + entry.player();
            yaml.set(path + ".name", entry.name());
            yaml.set(path + ".time", entry.time());
        }));
        String data = yaml.saveToString();
        io.execute(() -> write(data));
    }

    private void write(String data) {
        try {
            Files.writeString(file.toPath(), data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder times.yml : " + e.getMessage());
        }
    }

    public void close() {
        io.shutdown();
        try {
            io.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
