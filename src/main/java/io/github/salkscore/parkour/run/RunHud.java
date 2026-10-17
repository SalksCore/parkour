package io.github.salkscore.parkour.run;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class RunHud implements Runnable {

    private final ParkourPlugin plugin;

    public RunHud(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Session session : plugin.runs().sessions()) {
            Player player = Bukkit.getPlayer(session.player());
            if (player == null || plugin.bridge().hasMod(player)) continue;
            player.sendActionBar(plugin.messages().get("hud.actionbar",
                    Messages.time(session.elapsed()),
                    Messages.of("n", session.reached()),
                    Messages.of("total", session.course().size())));
        }
    }
}
