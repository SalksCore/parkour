package io.github.salkscore.parkour.run;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.course.Point;
import io.github.salkscore.parkour.util.Settings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class RunListener implements Listener {

    private final ParkourPlugin plugin;

    public RunListener(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        Player player = event.getPlayer();
        RunManager runs = plugin.runs();
        Session session = runs.get(player);

        Point point = plugin.courses().pointAt(event.getTo());
        if (point != null) {
            reach(player, session, point);
        } else if (session != null && fell(session.course(), event.getTo())) {
            runs.respawn(player);
        }
    }

    private void reach(Player player, Session session, Point point) {
        RunManager runs = plugin.runs();
        Course course = point.course();
        if (point.type() == Point.Type.START) {
            if (course.isReady()) runs.start(player, course);
            return;
        }
        if (session == null || session.course() != course) return;

        if (point.type() == Point.Type.CHECKPOINT) {
            runs.checkpoint(player, session, point.index());
        } else {
            runs.finish(player, session);
        }
    }

    private boolean fell(Course course, Location to) {
        if (course.fallY() != null && to.getY() < course.fallY()) return true;
        Settings settings = plugin.settings();
        Material type = to.getBlock().getType();
        return type == Material.WATER && settings.resetOnWater()
                || type == Material.LAVA && settings.resetOnLava();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.runs().restorePending(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.runs().stop(event.getPlayer(), StopReason.QUIT);
    }
}
