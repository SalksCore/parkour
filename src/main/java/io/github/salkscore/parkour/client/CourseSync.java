package io.github.salkscore.parkour.client;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.board.Leaderboard;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.util.Messages;
import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

final class CourseSync {

    private static final int TOP = 10;

    private CourseSync() {
    }

    static PacketWriter packet(ParkourPlugin plugin, Player player) {
        Collection<Course> courses = plugin.courses().all().stream()
                .filter(course -> course.start() != null)
                .toList();
        PacketWriter packet = new PacketWriter(PacketType.COURSES).writeVarInt(courses.size());
        for (Course course : courses) {
            write(plugin, player, course, packet);
        }
        return packet;
    }

    private static void write(ParkourPlugin plugin, Player player, Course course,
            PacketWriter packet) {
        Location start = course.start();
        packet.writeString(course.id())
                .writeString(name(course))
                .writeString(start.getWorld().getKey().toString());
        position(packet, start);

        Location board = course.board();
        packet.writeBoolean(board != null);
        if (board != null) position(packet, board);

        Leaderboard leaderboard = plugin.leaderboard();
        List<Leaderboard.Entry> top = leaderboard.top(course.id(), TOP);
        packet.writeVarInt(top.size());
        for (Leaderboard.Entry entry : top) {
            packet.writeString(entry.name()).writeLong(entry.time());
        }

        Leaderboard.Entry best = leaderboard.best(course.id(), player.getUniqueId());
        packet.writeLong(best == null ? -1L : best.time());
        packet.writeVarInt(leaderboard.rank(course.id(), player.getUniqueId()));
    }

    private static PacketWriter position(PacketWriter packet, Location loc) {
        return packet.writeDouble(loc.getX()).writeDouble(loc.getY()).writeDouble(loc.getZ());
    }

    static String name(Course course) {
        return GsonComponentSerializer.gson().serialize(Messages.parse(course.name()));
    }
}
