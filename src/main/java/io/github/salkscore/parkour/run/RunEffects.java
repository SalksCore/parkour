package io.github.salkscore.parkour.run;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.board.Leaderboard;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.util.Messages;
import io.github.salkscore.parkour.util.TimeFormat;
import java.time.Duration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

final class RunEffects {

    private static final Title.Times TIMES = Title.Times.times(
            Duration.ofMillis(100), Duration.ofMillis(1200), Duration.ofMillis(300));

    private final ParkourPlugin plugin;

    RunEffects(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    void start(Player player, Session session) {
        title(player, "run.start", Messages.course(session.course()));
        sound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1.6f);
    }

    void checkpoint(Player player, Session session) {
        TagResolver tags = TagResolver.resolver(
                Messages.course(session.course()),
                Messages.time(session.elapsed()),
                Messages.of("n", session.reached()),
                Messages.of("total", session.course().size()));

        title(player, "run.checkpoint", tags);
        plugin.messages().send(player, "run.checkpoint", tags);
        sound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.2f);
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 12, 0.4, 0.5, 0.4, 0);
    }

    void finish(Player player, Course course, Leaderboard.Result result) {
        Messages messages = plugin.messages();
        TagResolver tags = TagResolver.resolver(
                Messages.course(course),
                Messages.time(result.time()),
                Messages.of("rank", result.rank()),
                Messages.of("player", player.getName()));

        title(player, "run.finish", tags);
        messages.send(player, "run.finish", tags);
        if (result.previous() != null && result.improved()) {
            long diff = result.previous() - result.time();
            messages.send(player, "run.record", Messages.of("diff", TimeFormat.format(diff)));
        } else if (result.previous() != null) {
            String best = TimeFormat.format(result.previous());
            messages.send(player, "run.best", Messages.of("best", best));
        }
        if (result.improved() && result.rank() == 1) {
            messages.send(Bukkit.getServer(), "run.top", tags);
        }

        sound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f);
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 60, 0.5, 0.8, 0.5, 0.3);
    }

    void respawn(Player player) {
        sound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1.4f);
    }

    private void title(Player player, String key, TagResolver tags) {
        Messages messages = plugin.messages();
        player.showTitle(Title.title(messages.get(key + "-title", tags),
                messages.get(key + "-subtitle", tags), TIMES));
    }

    private void sound(Player player, Sound sound, float pitch) {
        player.playSound(player.getLocation(), sound, 0.8f, pitch);
    }
}
