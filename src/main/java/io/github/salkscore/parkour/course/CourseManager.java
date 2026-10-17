package io.github.salkscore.parkour.course;

import io.github.salkscore.parkour.ParkourPlugin;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class CourseManager {

    private static final Pattern ID = Pattern.compile("[a-z0-9_-]{1,32}");

    private final ParkourPlugin plugin;
    private final File file;
    private final Map<String, Course> courses = new LinkedHashMap<>();
    private final Map<BlockKey, Point> points = new HashMap<>();
    // cours dont le monde n'est pas chargé, gardés tels quels pour ne pas les perdre
    private final Map<String, Map<String, Object>> unloaded = new HashMap<>();

    public CourseManager(ParkourPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "courses.yml");
    }

    public void load() {
        courses.clear();
        unloaded.clear();
        ConfigurationSection root = YamlConfiguration.loadConfiguration(file)
                .getConfigurationSection("courses");
        if (root != null) {
            for (String id : root.getKeys(false)) {
                ConfigurationSection section = root.getConfigurationSection(id);
                if (section != null) load(id, section);
            }
        }
        reindex();
    }

    private void load(String id, ConfigurationSection section) {
        try {
            courses.put(id, CourseCodec.read(id, section));
        } catch (IllegalStateException | NumberFormatException e) {
            plugin.getLogger().warning("Parcours " + id + " ignoré : " + e.getMessage());
            unloaded.put(id, section.getValues(true));
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Course course : courses.values()) {
            CourseCodec.write(course, yaml.createSection("courses." + course.id()));
        }
        unloaded.forEach((id, values) -> values.forEach((key, value) -> {
            if (value instanceof ConfigurationSection) return;
            yaml.set("courses." + id + "." + key, value);
        }));

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder courses.yml : " + e.getMessage());
        }
        reindex();
    }

    private void reindex() {
        points.clear();
        for (Course course : courses.values()) {
            index(course.start(), new Point(course, Point.Type.START, 0));
            List<Location> checkpoints = course.checkpoints();
            for (int i = 0; i < checkpoints.size(); i++) {
                index(checkpoints.get(i), new Point(course, Point.Type.CHECKPOINT, i + 1));
            }
            index(course.finish(), new Point(course, Point.Type.FINISH, 0));
        }
    }

    private void index(Location loc, Point point) {
        if (loc != null) points.put(BlockKey.of(loc), point);
    }

    public Point pointAt(Location loc) {
        return points.get(BlockKey.of(loc));
    }

    public Course create(String id) {
        if (courses.containsKey(id) || unloaded.containsKey(id)) return null;
        Course course = new Course(id);
        courses.put(id, course);
        return course;
    }

    public void delete(String id) {
        courses.remove(id);
        save();
    }

    public Course get(String id) {
        return courses.get(id);
    }

    public Collection<Course> all() {
        return courses.values();
    }

    public Collection<String> ids() {
        return courses.keySet();
    }

    public static boolean isValidId(String id) {
        return ID.matcher(id).matches();
    }
}
