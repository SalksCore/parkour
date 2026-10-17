package io.github.salkscore.parkour.display;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.board.Leaderboard;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.util.Messages;
import io.github.salkscore.parkour.util.Settings;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;

final class CourseVisuals {

    private final ParkourPlugin plugin;
    private final Messages messages;

    CourseVisuals(ParkourPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.messages();
    }

    List<Visual> create(Course course) {
        Settings.Squares squares = plugin.settings().squares();
        List<Visual> list = new ArrayList<>();
        if (course.start() != null) list.add(new Square(course.start(), squares.start()));
        course.checkpoints().forEach(loc -> list.add(new Square(loc, squares.checkpoint())));
        if (course.finish() != null) list.add(new Square(course.finish(), squares.finish()));
        if (plugin.settings().holograms()) list.addAll(texts(course));
        return list;
    }

    private List<Visual> texts(Course course) {
        TagResolver tags = Messages.course(course);
        List<Visual> list = new ArrayList<>();
        if (course.start() != null) {
            list.add(text(above(course.start()), "holo.start", tags, true));
        }

        List<Location> checkpoints = course.checkpoints();
        for (int i = 0; i < checkpoints.size(); i++) {
            TagResolver n = TagResolver.resolver(tags, Messages.of("n", i + 1));
            list.add(text(above(checkpoints.get(i)), "holo.checkpoint", n, false));
        }

        if (course.finish() != null) {
            list.add(text(above(course.finish()), "holo.finish", tags, false));
        }
        if (course.board() != null) {
            list.add(new Hologram(course.board().clone(), () -> board(course), true));
        }
        return list;
    }

    private Hologram text(Location loc, String key, TagResolver tags, boolean drawnByMod) {
        return new Hologram(loc, () -> messages.get(key, tags), drawnByMod);
    }

    private Component board(Course course) {
        int size = plugin.settings().boardSize();
        List<Leaderboard.Entry> top = plugin.leaderboard().top(course.id(), size);
        List<Component> lines = new ArrayList<>();
        lines.add(messages.get("holo.board-header", Messages.course(course)));

        for (int i = 0; i < size; i++) {
            TagResolver rank = Messages.of("rank", i + 1);
            if (i >= top.size()) {
                lines.add(messages.get("holo.board-empty", rank));
                continue;
            }
            Leaderboard.Entry entry = top.get(i);
            lines.add(messages.get("holo.board-line", rank,
                    Messages.of("player", entry.name()), Messages.time(entry.time())));
        }
        return Component.join(JoinConfiguration.newlines(), lines);
    }

    private Location above(Location loc) {
        return loc.clone().add(0, plugin.settings().holoHeight(), 0);
    }
}
