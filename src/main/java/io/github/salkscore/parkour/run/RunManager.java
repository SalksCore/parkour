package io.github.salkscore.parkour.run;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.board.Leaderboard;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.util.Messages;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class RunManager {

    private final ParkourPlugin plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private final StateStore states;
    private final RunItems items;
    private final RunEffects effects;

    public RunManager(ParkourPlugin plugin) {
        this.plugin = plugin;
        File folder = new File(plugin.getDataFolder(), "states");
        this.states = new StateStore(folder, plugin.getLogger());
        this.items = new RunItems(plugin);
        this.effects = new RunEffects(plugin);
    }

    public void start(Player player, Course course) {
        Session current = get(player);
        if (current != null && current.course() == course) {
            current.restart();
            plugin.bridge().start(player, course);
            effects.start(player, current);
            return;
        }
        if (current != null) stop(player, StopReason.SWITCH);

        PlayerState state = PlayerState.capture(player);
        states.save(player.getUniqueId(), state);
        Session session = new Session(player.getUniqueId(), course, state, bar(course));
        sessions.put(player.getUniqueId(), session);

        session.bypass(() -> {
            player.getInventory().clear();
            player.clearActivePotionEffects();
            player.setGameMode(plugin.settings().gameMode());
            player.setFlying(false);
            player.setAllowFlight(false);
        });
        items.give(player);
        if (plugin.settings().bossbar() && !plugin.bridge().hasMod(player)) {
            player.showBossBar(session.bar());
        }
        plugin.bridge().start(player, course);
        effects.start(player, session);
    }

    public void checkpoint(Player player, Session session, int index) {
        if (index <= session.reached()) return;
        if (index > session.reached() + 1) {
            plugin.messages().send(player, "run.missed");
            return;
        }
        session.reach(index);
        plugin.bridge().checkpoint(player, index, session.elapsed());
        effects.checkpoint(player, session);
    }

    public void finish(Player player, Session session) {
        Course course = session.course();
        int missing = course.size() - session.reached();
        if (missing > 0) {
            plugin.messages().send(player, "run.missing", Messages.of("n", missing));
            return;
        }

        long time = session.elapsed();
        stop(player, StopReason.FINISH);
        Leaderboard.Result result = plugin.leaderboard().submit(course.id(), player, time);
        plugin.bridge().finish(player, time, result.rank());
        effects.finish(player, course, result);
        plugin.displays().refresh(course);

        if (course.spawn() == null) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && get(player) == null) player.teleport(course.exit());
        }, 30L);
    }

    public void respawn(Player player) {
        Session session = get(player);
        if (session == null) return;
        teleport(player, session, session.course().respawn(session.reached()));
        effects.respawn(player);
    }

    public void restart(Player player) {
        Session session = get(player);
        if (session == null) return;
        teleport(player, session, session.course().respawn(0));
        session.restart();
        plugin.bridge().start(player, session.course());
        effects.start(player, session);
    }

    public void stop(Player player, StopReason reason) {
        Session session = sessions.remove(player.getUniqueId());
        if (session == null) return;

        player.hideBossBar(session.bar());
        if (reason != StopReason.FINISH) plugin.bridge().stop(player);
        session.state().apply(player);
        states.delete(player.getUniqueId());
        if (reason.teleport()) player.teleport(session.course().exit());
        if (reason.message() != null) {
            plugin.messages().send(player, reason.message(), Messages.course(session.course()));
        }
    }

    public void stopAll(StopReason reason) {
        for (UUID id : List.copyOf(sessions.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) stop(player, reason);
        }
        sessions.clear();
    }

    public void stopAll(Course course, StopReason reason) {
        for (Session session : List.copyOf(sessions.values())) {
            Player player = Bukkit.getPlayer(session.player());
            if (player != null && session.course() == course) stop(player, reason);
        }
    }

    public void restorePending(Player player) {
        if (sessions.containsKey(player.getUniqueId())) return;
        PlayerState state = states.load(player.getUniqueId());
        if (state == null) return;
        state.apply(player);
        states.delete(player.getUniqueId());
    }

    private void teleport(Player player, Session session, Location target) {
        session.bypass(() -> {
            player.setVelocity(new Vector());
            player.setFallDistance(0f);
            player.setFireTicks(0);
            player.teleport(target);
        });
    }

    private BossBar bar(Course course) {
        return BossBar.bossBar(plugin.messages().get("hud.bossbar", Messages.course(course)), 0f,
                plugin.settings().barColor(), BossBar.Overlay.PROGRESS);
    }

    public Session get(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public Collection<Session> sessions() {
        return sessions.values();
    }

    public RunItems items() {
        return items;
    }
}
