package io.github.salkscore.parkour.holo;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.board.Leaderboard;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.util.Messages;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;

final class CourseHolograms {

    private final ParkourPlugin plugin;
    private final Messages messages;

    CourseHolograms(ParkourPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.messages();
    }

    List<Hologram> create(Course course) {
        TagResolver tags = Messages.course(course);
        List<Hologram> list = new ArrayList<>();
        if (course.start() != null) {
            list.add(new Hologram(above(course.start()), () -> messages.get("holo.start", tags)));
        }

        List<Location> checkpoints = course.checkpoints();
        for (int i = 0; i < checkpoints.size(); i++) {
            TagResolver n = TagResolver.resolver(tags, Messages.of("n", i + 1));
            Location loc = above(checkpoints.get(i));
            list.add(new Hologram(loc, () -> messages.get("holo.checkpoint", n)));
        }

        if (course.finish() != null) {
            list.add(new Hologram(above(course.finish()), () -> messages.get("holo.finish", tags)));
        }
        if (course.board() != null) {
            list.add(new Hologram(course.board().clone(), () -> board(course)));
        }
        return list;
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
