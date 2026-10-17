package io.github.salkscore.parkour.course;

import io.github.salkscore.parkour.util.Locations;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

final class CourseCodec {

    private CourseCodec() {
    }

    static Course read(String id, ConfigurationSection section) {
        Course course = new Course(id);
        course.name(section.getString("name", id));
        course.start(location(section, "start"));
        course.finish(location(section, "finish"));
        course.spawn(location(section, "spawn"));
        course.board(location(section, "board"));
        if (section.contains("fall-y")) course.fallY(section.getDouble("fall-y"));

        for (String raw : section.getStringList("checkpoints")) {
            course.checkpoints().add(Locations.parse(raw));
        }
        return course;
    }

    static void write(Course course, ConfigurationSection section) {
        section.set("name", course.name());
        set(section, "start", course.start());
        set(section, "finish", course.finish());
        set(section, "spawn", course.spawn());
        set(section, "board", course.board());
        section.set("fall-y", course.fallY());
        List<String> checkpoints = course.checkpoints().stream()
                .map(Locations::serialize)
                .toList();
        section.set("checkpoints", checkpoints);
    }

    private static Location location(ConfigurationSection section, String key) {
        String raw = section.getString(key);
        return raw == null ? null : Locations.parse(raw);
    }

    private static void set(ConfigurationSection section, String key, Location loc) {
        if (loc != null) section.set(key, Locations.serialize(loc));
    }
}
